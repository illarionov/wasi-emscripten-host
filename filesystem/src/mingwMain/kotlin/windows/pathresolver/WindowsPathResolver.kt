/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.pathresolver

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.fdresource.PreopenedDirectories
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal class WindowsPathResolver(
    private val fileDescriptors: FileDescriptorTable<FdResource>,
    private val fsLock: ReentrantLock,
) {
    private var currentWorkingDirectoryFd: FileDescriptor = FileDescriptorTable.INVALID_FD

    fun setupPreopenedDirectories(
        preopened: PreopenedDirectories,
    ) {
        preopened
            .preopenedDirectories
            .entries
            .forEachIndexed { index, (path: RealPath, ch: WindowsDirectoryChannel) ->
                val resource = WindowsDirectoryFdResource(ch)
                fileDescriptors[index + FileDescriptorTable.WASI_FIRST_PREOPEN_FD] = resource
            }

        currentWorkingDirectoryFd = preopened.currentWorkingDirectory.fold(
            ifLeft = { -1 },
        ) { channel: WindowsDirectoryChannel ->
            val fd = FileDescriptorTable.WASI_FIRST_PREOPEN_FD + preopened.preopenedDirectories.size
            fileDescriptors[fd] = WindowsDirectoryFdResource(channel)
            fd
        }
    }

    fun resolveBaseDirectory(
        directory: BaseDirectory,
    ): Either<ResolveRelativePathErrors, WindowsDirectoryChannel?> = when (directory) {
        CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != FileDescriptorTable.INVALID_FD) {
            getDirectoryChannel(currentWorkingDirectoryFd)
        } else {
            null.right()
        }

        is DirectoryFd -> getDirectoryChannel(directory.fd)
    }

    private fun getDirectoryChannel(fd: FileDescriptor): Either<ResolveRelativePathErrors, WindowsDirectoryChannel> {
        return fsLock.withLock {
            when (val fdResource = fileDescriptors[fd]) {
                null -> BadFileDescriptor("Directory File descriptor $fd is not open").left()
                is WindowsDirectoryFdResource -> fdResource.channel.right()
                else -> NotDirectory("FD $fd is not a directory").left()
            }
        }
    }
}
