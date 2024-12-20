/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.normalizeWindowsPath
import at.released.weh.filesystem.path.real.windows.normalizeWindowsSlashes
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtObjectManagerPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.path.withPathErrorAsCommonError
import at.released.weh.filesystem.path.withResolvePathError
import at.released.weh.filesystem.path.withResolvePathErrorAsCommonError
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess
import at.released.weh.filesystem.windows.nativefunc.open.useFileForAttributeAccess
import at.released.weh.filesystem.windows.path.NtPath
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import at.released.weh.filesystem.windows.win32api.windowsDosPathNameToNtPathName
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import platform.windows.HANDLE

internal class WindowsPathResolver(
    private val fileDescriptors: FileDescriptorTable<FdResource>,
    private val fdsLock: ReentrantLock,
    private var currentWorkingDirectoryFd: FileDescriptor,
    private val withRootAccess: Boolean,
) {
    fun <E : FileSystemOperationError, R : Any> executeWithOpenFileHandle(
        baseDirectory: BaseDirectory,
        path: VirtualPath,
        followSymlinks: Boolean = true,
        access: AttributeDesiredAccess = AttributeDesiredAccess.READ_ONLY,
        errorMapper: (OpenError) -> E,
        block: (HANDLE) -> Either<E, R>,
    ): Either<E, R> = getNtPath(baseDirectory, path)
        .mapLeft { errorMapper(it.toResolveRelativePathErrors()) }
        .flatMap { ntPath ->
            useFileForAttributeAccess(
                path = ntPath,
                followSymlinks = followSymlinks,
                access = access,
                errorMapper = errorMapper,
                block = block,
            )
        }

    fun getWindowsPath(
        directory: BaseDirectory,
        path: VirtualPath,
    ): Either<ResolveRelativePathErrors, WindowsRealPath> = either {
        if (!withRootAccess && path.isAbsolute()) {
            raise(NotCapable("Can not open absolute path"))
        }

        val baseDirectoryHandle = getBaseDirectory(directory)
            .withResolvePathErrorAsCommonError()
            .bind()
            ?.handle

        return when (baseDirectoryHandle) {
            null -> path.asNonRelativeWindowsPath().withPathErrorAsCommonError()
            else -> {
                if (!withRootAccess) {
                    // Validate that the path is not outside the base directory
                    normalizeWindowsPath(path.toString()).withPathErrorAsCommonError().bind()
                }

                baseDirectoryHandle.getFinalPath()
                    .mapLeft(GetFinalPathError::toResolveRelativePathError)
                    .flatMap { directory -> directory.append(path.toString()).withPathErrorAsCommonError() }
            }
        }
    }

    fun getNtPath(
        directory: BaseDirectory,
        path: VirtualPath,
    ): Either<ResolvePathError, NtPath> = either {
        if (!withRootAccess && path.isAbsolute()) {
            raise(PathError.AbsolutePath("Can not open absolute path"))
        }
        val baseDirectoryHandle = getBaseDirectory(directory).bind()?.handle
        return if (baseDirectoryHandle == null) {
            resolveAbsoluteNtPath(path)
        } else {
            resolveRelativeNtPath(baseDirectoryHandle, path)
        }
    }

    private fun resolveAbsoluteNtPath(
        path: VirtualPath,
    ): Either<ResolvePathError, NtPath> = either {
        if (!withRootAccess) {
            raise(PathError.FileDescriptorNotOpen("Can not open base directory"))
        }
        if (!path.isAbsolute()) {
            raise(PathError.InvalidPathFormat("Path is not absolute"))
        }

        return path.asNonRelativeWindowsPath()
            .withResolvePathError()
            .flatMap(WindowsRealPath::toNtPath)
            .map(NtPath::Absolute)
    }

    private fun resolveRelativeNtPath(
        baseHandle: HANDLE,
        path: VirtualPath,
    ): Either<ResolvePathError, NtPath> = either {
        if (path.isAbsolute()) {
            raise(PathError.AbsolutePath("Path should not absolute"))
        }
        val ntRelativePath: WindowsNtRelativePath = path.toRelativeNtPath().bind()
        NtPath.Relative(baseHandle, ntRelativePath)
    }

    private fun VirtualPath.toRelativeNtPath(): Either<ResolvePathError, WindowsNtRelativePath> {
        if (isAbsolute()) {
            return PathError.AbsolutePath("Path should not be absolute").left()
        }
        return normalizeWindowsPath(toString())
            .flatMap { canonizedPath -> WindowsNtRelativePath.create(canonizedPath) }
            .mapLeft { it.toResolvePathError() }
    }

    /**
     * Guess Windows real path from [VirtualPath].
     *
     * Examples:
     * * Virtual path: `/Windows/System32`, Windows Real Path: `\Windows\System32`
     * * Virtual path: `D:/Users/Public`, Windows real path: `D:\Users\Public`
     */
    private fun VirtualPath.asNonRelativeWindowsPath(): Either<PathError, WindowsRealPath> {
        return WindowsRealPath.create(normalizeWindowsSlashes(this.toString()))
            .flatMap(WindowsRealPath::normalize)
    }

    fun getBaseDirectory(
        directory: BaseDirectory,
    ): Either<ResolvePathError, WindowsDirectoryChannel?> = when (directory) {
        BaseDirectory.CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != FileDescriptorTable.INVALID_FD) {
            getDirectoryChannel(currentWorkingDirectoryFd)
        } else {
            null.right()
        }

        is BaseDirectory.DirectoryFd -> getDirectoryChannel(directory.fd)
    }

    private fun getDirectoryChannel(fd: FileDescriptor): Either<ResolvePathError, WindowsDirectoryChannel> {
        return fdsLock.withLock {
            when (val fdResource = fileDescriptors[fd]) {
                null -> PathError.FileDescriptorNotOpen("Directory File descriptor $fd is not open").left()
                is WindowsDirectoryFdResource -> fdResource.channel.right()
                else -> PathError.NotDirectory("FD $fd is not a directory").left()
            }
        }
    }
}

internal fun WindowsRealPath.toNtPath(): Either<ResolvePathError, WindowsNtObjectManagerPath> {
    return windowsDosPathNameToNtPathName(this.kString)
}
