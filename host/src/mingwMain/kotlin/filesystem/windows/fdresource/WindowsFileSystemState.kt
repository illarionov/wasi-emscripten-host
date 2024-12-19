/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.toFileDescriptorMap
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.fdresource.WindowsFileFdResource.WindowsFileChannel
import at.released.weh.filesystem.windows.pathresolver.WindowsPathResolver
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.IOException

internal class WindowsFileSystemState private constructor(
    stdio: StandardInputOutput,
    internal val isRootAccessAllowed: Boolean,
    preopenedDirectories: Map<FileDescriptor, WindowsDirectoryFdResource>,
    currentWorkingDirectory: FileDescriptor,
) : AutoCloseable {
    internal val fdsLock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable(
        stdio.toFileDescriptorMap() + preopenedDirectories,
    )
    val pathResolver = WindowsPathResolver(fileDescriptors, fdsLock, currentWorkingDirectory)

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = fdsLock.withLock {
        fileDescriptors[fd]
    }

    fun addFile(
        handle: WindowsFileChannel,
    ): Either<Nfile, Pair<FileDescriptor, WindowsFileFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            WindowsFileFdResource(handle).right()
        }
    }

    fun addDirectory(
        handle: WindowsDirectoryChannel,
    ): Either<Nfile, Pair<FileDescriptor, WindowsDirectoryFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            WindowsDirectoryFdResource(handle).right()
        }
    }

    fun remove(
        @IntFileDescriptor fd: FileDescriptor,
    ): Either<BadFileDescriptor, FdResource> = fdsLock.withLock {
        return fileDescriptors.release(fd)
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithResource(
        fd: FileDescriptor,
        crossinline block: (fdResource: FdResource) -> Either<E, R>,
    ): Either<E, R> {
        @Suppress("UNCHECKED_CAST")
        val resource: FdResource = get(fd) ?: return (BadFileDescriptor(fileDescriptorNotOpenMessage(fd)) as E).left()
        return block(resource)
    }

    fun renumber(
        @IntFileDescriptor fromFd: FileDescriptor,
        @IntFileDescriptor toFd: FileDescriptor,
    ): Either<BadFileDescriptor, Unit> {
        var toResource: FdResource? = null
        fdsLock.withLock {
            val fromResource = fileDescriptors[fromFd]
            toResource = fileDescriptors[toFd]

            if (fromResource == null) {
                return BadFileDescriptor("Incorrect fromFd").left()
            }
            if (toResource == null) {
                return BadFileDescriptor("Incorrect toFd").left()
            }

            fileDescriptors.release(fromFd).onLeft { error("Unexpected error `$it`") }
            fileDescriptors.release(toFd).onLeft { error("Unexpected error `$it`") }
            fileDescriptors[toFd] = fromResource
        }
        toResource?.close()?.onLeft { /* ignore */ }
        return Unit.right()
    }

    override fun close() {
        val fdResources: List<FdResource> = fdsLock.withLock {
            fileDescriptors.drain()
        }
        for (fd in fdResources) {
            fd.close().onLeft { /* ignore error */ }
        }
    }

    companion object {
        @Throws(IOException::class)
        fun create(
            stdio: StandardInputOutput,
            isRootAccessAllowed: Boolean,
            cwd: String,
            preopenedDirectories: List<PreopenedDirectory>,
        ): WindowsFileSystemState {
            val (cwdResult, directories) = WindowsBatchDirectoryOpener.preopen(cwd, preopenedDirectories)
                .getOrElse { openError ->
                    throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
                }

            val preopens: MutableMap<Int, WindowsDirectoryFdResource> =
                directories.indices.associateTo(mutableMapOf()) { index ->
                    val channel = directories[index]
                    index + FileDescriptorTable.WASI_FIRST_PREOPEN_FD to WindowsDirectoryFdResource(channel)
                }

            val currentWorkingDirectoryFd = cwdResult.fold(
                ifLeft = { -1 },
            ) { channel: WindowsDirectoryChannel ->
                val fd = FileDescriptorTable.WASI_FIRST_PREOPEN_FD + directories.size
                preopens[fd] = WindowsDirectoryFdResource(channel)
                fd
            }

            return WindowsFileSystemState(stdio, isRootAccessAllowed, preopens, currentWorkingDirectoryFd)
        }
    }
}
