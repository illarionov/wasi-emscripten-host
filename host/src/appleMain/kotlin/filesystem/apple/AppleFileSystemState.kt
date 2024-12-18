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
import at.released.weh.filesystem.apple.fdresource.AppleDirectoryPreopener
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.INVALID_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.toFileDescriptorMap
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.path.real.posix.PosixPathConverter
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
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
) : AutoCloseable {
    private val fdsLock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable(
        stdio.toFileDescriptorMap() + preopenedDirectories,
    )
    val pathResolver = PathResolver(fileDescriptors, fdsLock, currentWorkingDirectoryFd)

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
        nativeFd: NativeDirectoryFd,
        virtualPath: VirtualPath,
        isPreopened: Boolean = false,
        rights: FdRightsBlock,
    ): Either<Nfile, Pair<FileDescriptor, AppleDirectoryFdResource>> = fdsLock.withLock {
        fileDescriptors.allocate { _ ->
            AppleDirectoryFdResource(nativeFd, isPreopened, virtualPath, rights).right()
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

    inline fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        crossinline block: (path: PosixRealPath, baseDirectory: NativeDirectoryFd) -> Either<E, R>,
    ): Either<E, R> {
        val realPath = PosixPathConverter.toRealPath(path)
            .getOrElse { bfe ->
                @Suppress("UNCHECKED_CAST")
                return (bfe as E).left()
            }

        val nativeFd: NativeDirectoryFd = pathResolver.resolveNativeDirectoryFd(baseDirectory)
            .getOrElse { bfe ->
                @Suppress("UNCHECKED_CAST")
                return (bfe as E).left()
            }
        return block(realPath, nativeFd)
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

    // TODO: merge with linux
    class PathResolver(
        private val fileDescriptors: FileDescriptorTable<FdResource>,
        private val fsLock: ReentrantLock,
        private var currentWorkingDirectoryFd: FileDescriptor = INVALID_FD,
    ) {
        fun resolveNativeDirectoryFd(
            directory: BaseDirectory,
        ): Either<FileSystemOperationError, NativeDirectoryFd> = when (directory) {
            CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != INVALID_FD) {
                getDirectoryNativeFd(currentWorkingDirectoryFd)
            } else {
                BadFileDescriptor("Current directory not opened").left()
            }

            is DirectoryFd -> getDirectoryNativeFd(directory.fd)
        }

        private fun getDirectoryNativeFd(
            fd: FileDescriptor,
        ): Either<FileSystemOperationError, NativeDirectoryFd> = fsLock.withLock {
            when (val fdResource = fileDescriptors[fd]) {
                null -> BadFileDescriptor("Directory File descriptor $fd is not open").left()
                !is AppleDirectoryFdResource -> NotDirectory("FD $fd is not a directory").left()
                else -> fdResource.nativeFd.right()
            }
        }
    }

    companion object {
        @Throws(IOException::class)
        fun create(
            stdio: StandardInputOutput,
            currentWorkingDirectory: String?,
            preopenedDirectories: List<PreopenedDirectory>,
        ): AppleFileSystemState {
            val (cwdResult, directories) = AppleDirectoryPreopener.preopen(
                currentWorkingDirectory,
                preopenedDirectories,
            )
                .getOrElse { openError ->
                    throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
                }

            val preopened: MutableMap<FileDescriptor, AppleDirectoryFdResource> =
                directories.indices.associateTo(mutableMapOf()) { index ->
                    index + WASI_FIRST_PREOPEN_FD to directories[index]
                }
            val currentWorkingDirectoryFd: FileDescriptor = cwdResult.fold(
                ifLeft = { -1 },
            ) { resource: AppleDirectoryFdResource ->
                val fd = WASI_FIRST_PREOPEN_FD + directories.size
                preopened[fd] = resource
                fd
            }

            return AppleFileSystemState(stdio, preopened, currentWorkingDirectoryFd)
        }
    }
}
