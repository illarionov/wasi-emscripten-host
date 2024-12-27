/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.INVALID_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toResolvePathError
import at.released.weh.filesystem.path.virtual.VirtualPath
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal class PosixPathResolver(
    private val fileDescriptors: FileDescriptorTable<FdResource>,
    private val fsLock: ReentrantLock,
    private var currentWorkingDirectoryFd: FileDescriptor = INVALID_FD,
) {
    fun getBaseDirectory(
        directory: BaseDirectory,
    ): Either<ResolvePathError, PosixDirectoryChannel> = when (directory) {
        CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != INVALID_FD) {
            getDirectoryChannel(currentWorkingDirectoryFd)
        } else {
            FileDescriptorNotOpen("Current directory not open").left()
        }

        is DirectoryFd -> getDirectoryChannel(directory.fd)
    }

    private fun getDirectoryChannel(
        fd: FileDescriptor,
    ): Either<ResolvePathError, PosixDirectoryChannel> = fsLock.withLock {
        when (val fdResource = fileDescriptors[fd]) {
            null -> FileDescriptorNotOpen("Directory File descriptor $fd is not open").left()
            !is PosixDirectoryFdResource -> PathError.NotDirectory("FD $fd is not a directory").left()
            else -> fdResource.channel.right()
        }
    }
}
