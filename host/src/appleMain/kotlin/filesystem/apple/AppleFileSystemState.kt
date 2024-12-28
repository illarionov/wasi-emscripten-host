/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.fdresource.AppleDirectoryFdResource
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.apple.nativefunc.appleOpenRaw
import at.released.weh.filesystem.apple.nativefunc.appleReadLink
import at.released.weh.filesystem.apple.nativefunc.appleStat
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.toFileDescriptorMap
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.fdresource.DirectFileSystemActionExecutor
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor
import at.released.weh.filesystem.posix.fdresource.NonSystemFileSystemActionExecutor
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

internal class AppleFileSystemState private constructor(
    stdio: StandardInputOutput,
    preopenedDirectories: Map<FileDescriptor, FdResource>,
    currentWorkingDirectoryFd: FileDescriptor,
    isRootAccessAllowed: Boolean,
) : AutoCloseable {
    internal val fdsLock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable(
        stdio.toFileDescriptorMap() + preopenedDirectories,
    )
    val pathResolver = PosixPathResolver(fileDescriptors, fdsLock, currentWorkingDirectoryFd)
    val fsExecutor: FileSystemActionExecutor = if (isRootAccessAllowed) {
        DirectFileSystemActionExecutor(pathResolver)
    } else {
        NonSystemFileSystemActionExecutor(
            pathResolver = pathResolver,
            openDirectoryFunction = { base, path, _ ->
                appleOpenRaw(base, path, O_DIRECTORY or O_NOFOLLOW, 0, null).map(::NativeDirectoryFd)
            },
            statFunction = { base, path -> appleStat(base, path, false) },
            readLinkFunction = ::appleReadLink,
        )
    }

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = fdsLock.withLock {
        fileDescriptors[fd]
    }

    fun addFile(
        channel: NativeFileChannel,
    ): Either<Nfile, Pair<FileDescriptor, AppleFileFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            AppleFileFdResource(channel).right()
        }
    }

    fun addDirectory(
        channel: PosixDirectoryChannel,
    ): Either<Nfile, Pair<FileDescriptor, PosixDirectoryFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            AppleDirectoryFdResource(channel).right()
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
            isRootAccessAllowed: Boolean,
        ): AppleFileSystemState {
            val (cwdResult, directories) = PosixDirectoryPreopener(::appleOpenRaw).preopen(
                currentWorkingDirectory,
                preopenedDirectories,
            ).getOrElse { openError ->
                throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
            }

            val preopened: MutableMap<FileDescriptor, PosixDirectoryFdResource> =
                directories.indices.associateTo(mutableMapOf()) { index ->
                    index + WASI_FIRST_PREOPEN_FD to AppleDirectoryFdResource(directories[index])
                }
            val currentWorkingDirectoryFd: FileDescriptor = cwdResult.fold(
                ifLeft = { -1 },
            ) { channel ->
                val fd = WASI_FIRST_PREOPEN_FD + directories.size
                preopened[fd] = AppleDirectoryFdResource(channel)
                fd
            }

            return AppleFileSystemState(stdio, preopened, currentWorkingDirectoryFd, isRootAccessAllowed)
        }
    }
}
