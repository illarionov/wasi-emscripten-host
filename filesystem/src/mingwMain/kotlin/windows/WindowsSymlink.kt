/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_ONLY
import at.released.weh.filesystem.windows.nativefunc.open.executeWithOpenFileHandle
import at.released.weh.filesystem.windows.path.WindowsPathConverter
import at.released.weh.filesystem.windows.pathresolver.resolveRealPath
import at.released.weh.filesystem.windows.win32api.SymlinkType
import at.released.weh.filesystem.windows.win32api.SymlinkType.SYMLINK_TO_FILE
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.windowsCreateSymbolicLink

internal class WindowsSymlink(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> = either {
        val virtualOldPath = VirtualPath.of(input.oldPath).mapLeft { InvalidArgument(it.message) }.bind()

        validateSymlinkTarget(virtualOldPath, input.allowAbsoluteOldPath).bind()

        val virtualNewPath: VirtualPath = VirtualPath.of(input.newPath).mapLeft { InvalidArgument(it.message) }.bind()
        val newPath: RealPath = fsState.pathResolver.resolveRealPath(input.newPathBaseDirectory, virtualNewPath).bind()

        val windowsOldPath = WindowsPathConverter.convertToRealPath(virtualOldPath)
        val symlinkType = getSymlinkTypeByTargetType(input.newPathBaseDirectory, windowsOldPath).bind()

        return windowsCreateSymbolicLink(windowsOldPath, newPath, symlinkType)
    }

    private fun validateSymlinkTarget(
        target: VirtualPath,
        allowAbsolutePaths: Boolean,
    ): Either<SymlinkError, Unit> = either {
        if (!allowAbsolutePaths && target.isAbsolute()) {
            raise(InvalidArgument("link destination should be relative"))
        }
    }

    private fun getSymlinkTypeByTargetType(
        baseDirectory: BaseDirectory,
        target: RealPath,
    ): Either<SymlinkError, SymlinkType> {
        val symlinkType: Either<SymlinkError, SymlinkType> = fsState.executeWithOpenFileHandle(
            baseDirectory = baseDirectory,
            path = target,
            followSymlinks = false,
            access = READ_ONLY,
            errorMapper = { it.toSymlinkError() },
        ) { handle ->
            handle.getFileAttributeTagInfo()
                .fold(
                    ifLeft = { _ -> SYMLINK_TO_FILE },
                    ifRight = { attributeTagInfo ->
                        if (attributeTagInfo.fileAttributes.isDirectory) {
                            SymlinkType.SYMLINK_TO_DIRECTORY
                        } else {
                            SYMLINK_TO_FILE
                        }
                    },
                ).right()
        }
        return symlinkType.getOrElse { SYMLINK_TO_FILE }.right()
    }

    @Suppress("CyclomaticComplexMethod")
    private fun OpenError.toSymlinkError(): SymlinkError = when (this) {
        is AccessDenied -> this
        is Again -> IoError(this.message)
        is BadFileDescriptor -> this
        is DiskQuota -> this
        is Exists -> this
        is Interrupted -> IoError(this.message)
        is InvalidArgument -> this
        is IoError -> this
        is Mfile -> BadFileDescriptor(this.message)
        is Mlink -> BadFileDescriptor(this.message)
        is NameTooLong -> this
        is Nfile -> NoEntry(this.message)
        is NoEntry -> this
        is NoSpace -> this
        is NotCapable -> this
        is NotDirectory -> this
        is NotSupported -> InvalidArgument(this.message)
        is Nxio -> NoEntry(this.message)
        is PathIsDirectory -> IoError(this.message)
        is PermissionDenied -> this
        is ReadOnlyFileSystem -> this
        is TextFileBusy -> IoError(this.message)
        is TooManySymbolicLinks -> this
    }
}
