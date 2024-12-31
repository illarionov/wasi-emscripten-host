/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.linux.native.linuxOpenRaw
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.posix.fdresource.DirectFileSystemActionExecutor
import at.released.weh.filesystem.posix.fdresource.NativeStdioFileFdResource.Companion.toFileDescriptorMapWithNativeFd
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryFdResource
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryPreopener
import at.released.weh.filesystem.posix.fdresource.PosixPathResolver
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.IOException

internal class LinuxFileSystemState private constructor(
    stdio: StandardInputOutput,
    preopenedDirectories: Map<FileDescriptor, FdResource>,
    currentWorkingDirectory: FileDescriptor,
) : AutoCloseable {
    internal val fdsLock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable(
        stdio.toFileDescriptorMapWithNativeFd() + preopenedDirectories,
    )
    val pathResolver = PosixPathResolver(fileDescriptors, fdsLock, currentWorkingDirectory)
    val fsExecutor = DirectFileSystemActionExecutor(pathResolver)

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = fdsLock.withLock {
        fileDescriptors[fd]
    }

    fun addFile(
        channel: NativeFileChannel,
    ): Either<Nfile, Pair<FileDescriptor, LinuxFileFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            LinuxFileFdResource(channel).right()
        }
    }

    fun addDirectory(
        channel: PosixDirectoryChannel,
    ): Either<Nfile, Pair<FileDescriptor, PosixDirectoryFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            LinuxDirectoryFdResource(channel).right()
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
            currentWorkingDirectory: String?,
            preopenedDirectories: List<PreopenedDirectory>,
        ): LinuxFileSystemState {
            val (cwdResult, directories) = PosixDirectoryPreopener(::linuxOpenRaw).preopen(
                currentWorkingDirectory,
                preopenedDirectories,
            ).getOrElse { openError ->
                throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
            }

            val preopened: MutableMap<FileDescriptor, PosixDirectoryFdResource> =
                directories.indices.associateTo(mutableMapOf()) { index ->
                    index + WASI_FIRST_PREOPEN_FD to LinuxDirectoryFdResource(directories[index])
                }

            val currentWorkingDirectoryFd: FileDescriptor = cwdResult.fold(
                ifLeft = { -1 },
            ) { channel ->
                val fd = WASI_FIRST_PREOPEN_FD + directories.size
                preopened[fd] = LinuxDirectoryFdResource(channel)
                fd
            }

            return LinuxFileSystemState(stdio, preopened, currentWorkingDirectoryFd)
        }
    }
}
