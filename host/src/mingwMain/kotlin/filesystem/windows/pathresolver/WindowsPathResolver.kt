/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.pathresolver

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.PathError.AbsolutePath
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.normalizeWindowsPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.path.NtPath
import at.released.weh.filesystem.windows.path.resolveAbsolutePath
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import platform.windows.HANDLE

internal class WindowsPathResolver(
    private val fileDescriptors: FileDescriptorTable<FdResource>,
    private val fsLock: ReentrantLock,
    private var currentWorkingDirectoryFd: FileDescriptor = FileDescriptorTable.INVALID_FD,
    private val allowRootAccess: Boolean = false,
) {
    fun resolveNtPath(
        directory: BaseDirectory,
        path: VirtualPath,
    ): Either<ResolvePathError, NtPath> = either {
        if (!allowRootAccess && path.isAbsolute()) {
            raise(AbsolutePath("Can not open absolute path"))
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
        if (!allowRootAccess) {
            raise(FileDescriptorNotOpen("Can not open base directory"))
        }
        if (!path.isAbsolute()) {
            raise(PathError.InvalidPathFormat("Path is not absolute"))
        }

        raise(PathError.IoError("Not implemented yet"))
    }

    private fun resolveRelativeNtPath(
        baseHandle: HANDLE,
        path: VirtualPath,
    ): Either<ResolvePathError, NtPath> = either {
        if (path.isAbsolute()) {
            raise(AbsolutePath("Path should not absolute"))
        }
        val ntRelativePath = toRelativeNtPath(path).bind()
        NtPath.Relative(baseHandle, ntRelativePath)
    }

    fun resolveRealPath(
        directory: BaseDirectory,
        path: VirtualPath,
    ): Either<ResolveRelativePathErrors, WindowsRealPath> {
        return this.resolveNtPath(directory, path)
            .mapLeft<ResolveRelativePathErrors>(ResolvePathError::toCommonError)
            .flatMap(NtPath::resolveAbsolutePath)
    }

    private fun toRelativeNtPath(path: VirtualPath): Either<ResolvePathError, WindowsNtRelativePath> {
        if (path.isAbsolute()) {
            return AbsolutePath("Path should not be absolute").left()
        }
        return normalizeWindowsPath(path.toString())
            .flatMap { canonizedPath -> WindowsNtRelativePath.create(canonizedPath) }
            .mapLeft { it as ResolvePathError }
    }

    fun getBaseDirectory(
        directory: BaseDirectory,
    ): Either<ResolvePathError, WindowsDirectoryChannel?> = when (directory) {
        CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != FileDescriptorTable.INVALID_FD) {
            getDirectoryChannel(currentWorkingDirectoryFd)
        } else {
            null.right()
        }

        is DirectoryFd -> getDirectoryChannel(directory.fd)
    }

    private fun getDirectoryChannel(fd: FileDescriptor): Either<ResolvePathError, WindowsDirectoryChannel> {
        return fsLock.withLock {
            when (val fdResource = fileDescriptors[fd]) {
                null -> FileDescriptorNotOpen("Directory File descriptor $fd is not open").left()
                is WindowsDirectoryFdResource -> fdResource.channel.right()
                else -> PathError.NotDirectory("FD $fd is not a directory").left()
            }
        }
    }
}
