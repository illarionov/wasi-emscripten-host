/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.SymlinkResolver
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter.normalizeWindowsSlashes
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toOpenError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.withPathErrorAsCommonError
import at.released.weh.filesystem.windows.path.ResolverPath
import at.released.weh.filesystem.windows.path.toNtPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.NtCreateFileResult
import at.released.weh.filesystem.windows.win32api.createfile.NtPath
import at.released.weh.filesystem.windows.win32api.createfile.toOpenError
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtCreateFile
import at.released.weh.filesystem.windows.win32api.deviceiocontrol.getReparsePoint
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_REPARSE_POINT
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.FILE_TRAVERSE
import platform.windows.HANDLE

internal fun windowsNtCreateFileEx(
    path: ResolverPath,
    withRootAccess: Boolean = false,
    desiredAccess: Int = FILE_GENERIC_WRITE,
    fileAttributes: Int = FILE_ATTRIBUTE_NORMAL,
    shareAccess: Int = FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE,
    createDisposition: Int = FILE_OPEN,
    createOptions: Int = FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT,
): Either<OpenError, HANDLE> = either {
    if (withRootAccess) {
        val ntPath = path.toNtPath().withPathErrorAsCommonError().bind()
        windowsNtCreateFile(
            ntPath = ntPath,
            desiredAccess = desiredAccess,
            fileAttributes = fileAttributes,
            shareAccess = shareAccess,
            createDisposition = createDisposition,
            createOptions = createOptions,
        ).mapLeft(NtCreateFileResult::toOpenError).bind()
    } else {
        val relativePath = path as? ResolverPath.RelativePath ?: raise(NotCapable("Can not open absolute path"))
        WindowsSymlinkResolver(
            base = relativePath.handle,
            path = relativePath.path,
            desiredAccess = desiredAccess,
            fileAttributes = fileAttributes,
            shareAccess = shareAccess,
            createDisposition = createDisposition,
            createOptions = createOptions,
        ).resolve().mapLeft(ResolvePathError::toOpenError).bind()
    }
}

internal class WindowsSymlinkResolver(
    base: HANDLE,
    path: VirtualPath,
    private val desiredAccess: Int = FILE_GENERIC_WRITE,
    private val fileAttributes: Int = FILE_ATTRIBUTE_NORMAL,
    private val shareAccess: Int = FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE,
    private val createDisposition: Int = FILE_OPEN,
    private val createOptions: Int = FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT,
) {
    private val resolver: SymlinkResolver<HANDLE> = SymlinkResolver(
        base = Directory(base),
        path = path,
        followBasenameSymlink = createOptions and FILE_OPEN_REPARSE_POINT != FILE_OPEN_REPARSE_POINT,
        openFunction = ::windowsOpen,
        closeFunction = ::windowsClose,
    )

    fun resolve(): Either<ResolvePathError, HANDLE> {
        return resolver.resolve().map { it.handle }
    }

    private fun windowsOpen(
        base: Directory<HANDLE>,
        component: String,
        isBasename: Boolean,
    ): Either<OpenError, Subcomponent<HANDLE>> = either {
        val ntPath = NtPath.Relative(
            base.handle,
            component.toNtRelativePath().bind(),
        )
        val newHandle: HANDLE = if (!isBasename) {
            windowsNtCreateFile(
                ntPath = ntPath,
                desiredAccess = FILE_LIST_DIRECTORY or FILE_READ_ATTRIBUTES or FILE_TRAVERSE,
                fileAttributes = FILE_ATTRIBUTE_DIRECTORY,
                createDisposition = FILE_OPEN,
                createOptions = FILE_DIRECTORY_FILE or FILE_OPEN_REPARSE_POINT,
                shareAccess = shareAccess,
            )
        } else {
            windowsNtCreateFile(
                ntPath = ntPath,
                desiredAccess = desiredAccess,
                fileAttributes = fileAttributes,
                createDisposition = createDisposition,
                createOptions = createOptions or FILE_OPEN_REPARSE_POINT,
                shareAccess = shareAccess,
            )
        }.mapLeft(NtCreateFileResult::toOpenError).bind()

        val attributes = newHandle.getFileAttributeTagInfo()
            .mapLeft(StatError::toOpenError)
            .bind()

        when {
            attributes.isSymlink -> {
                val target = newHandle.getReparsePoint()
                    .mapLeft { it.toOpenError() }
                    .flatMap { WindowsPathConverter.toVirtualPath(it).withPathErrorAsCommonError() }
                    .bind()
                Subcomponent.Symlink(newHandle, target)
            }

            attributes.fileAttributes.isDirectory -> Subcomponent.Directory(newHandle)
            else -> Subcomponent.Other(newHandle)
        }
    }

    private fun windowsClose(
        component: Subcomponent<HANDLE>,
    ): Either<CloseError, Unit> {
        return component.handle.close()
    }

    private fun String.toNtRelativePath(): Either<OpenError, WindowsNtRelativePath> = if (this == ".") {
        WindowsNtRelativePath.CURRENT.right()
    } else {
        WindowsNtRelativePath.create(normalizeWindowsSlashes(this)).withPathErrorAsCommonError()
    }

    private fun ReadLinkError.toOpenError(): OpenError = when (this) {
        is AccessDenied -> this
        is BadFileDescriptor -> this
        is InvalidArgument -> this
        is IoError -> this
        is NameTooLong -> this
        is Nfile -> this
        is NoEntry -> this
        is NotCapable -> this
        is NotDirectory -> this
        is TooManySymbolicLinks -> this
    }
}
