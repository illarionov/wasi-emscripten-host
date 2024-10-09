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
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.INVALID_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.initStdioDescriptors
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.stdio.StandardInputOutput
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.IOException

internal class LinuxFileSystemState private constructor(
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    preopenedDirectories: PreopenedDirectories,
) : AutoCloseable {
    private val lock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable()
    val pathResolver = PathResolver(fileDescriptors, lock)

    init {
        initStdioDescriptors(fileDescriptors, stdio)
        pathResolver.setupPreopenedDirectories(preopenedDirectories)
    }

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = lock.withLock {
        fileDescriptors[fd]
    }

    fun add(
        nativeFd: NativeFileFd,
    ): Either<Nfile, Pair<FileDescriptor, LinuxFileFdResource>> = lock.withLock {
        fileDescriptors.allocate { _ ->
            LinuxFileFdResource(nativeFd = nativeFd)
        }
    }

    fun remove(
        @IntFileDescriptor fd: FileDescriptor,
    ): Either<BadFileDescriptor, FdResource> = lock.withLock {
        return fileDescriptors.release(fd)
            .onRight { resource ->
                pathResolver.onFileDescriptorRemovedUnsafe(resource)
            }
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithResource(
        fd: FileDescriptor,
        crossinline block: (fdResource: FdResource) -> Either<E, R>,
    ): Either<E, R> {
        @Suppress("UNCHECKED_CAST")
        val resource: FdResource = get(fd) ?: return (BadFileDescriptor(fileDescriptorNotOpenMessage(fd)) as E).left()
        return block(resource)
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithBaseDirectoryResource(
        baseDirectory: BaseDirectory,
        crossinline block: (directoryNativeFdOrCwd: NativeDirectoryFd) -> Either<E, R>,
    ): Either<E, R> {
        val nativeFd: NativeDirectoryFd = pathResolver.resolveNativeDirectoryFd(baseDirectory)
            .getOrElse { bfe ->
                @Suppress("UNCHECKED_CAST")
                return (bfe as E).left()
            }
        return block(nativeFd)
    }

    override fun close() {
        val fdResources: List<FdResource> = lock.withLock {
            fileDescriptors.drain()
        }
        for (fd in fdResources) {
            fd.close().onLeft { /* ignore error */ }
        }
    }

    class PathResolver(
        private val fileDescriptors: FileDescriptorTable<FdResource>,
        private val fsLock: ReentrantLock,
    ) {
        private val openedDirectories: MutableMap<String, FdResource> = mutableMapOf()
        private var currentWorkingDirectoryFd: FileDescriptor = INVALID_FD

        fun setupPreopenedDirectories(
            preopened: PreopenedDirectories,
        ) {
            require(openedDirectories.isEmpty())

            preopened.preopenedDirectories.entries
                .forEachIndexed { index, (path: RealPath, resource: LinuxDirectoryFdResource) ->
                    fileDescriptors[index + WASI_FIRST_PREOPEN_FD] = resource
                    openedDirectories[path] = resource
                }

            currentWorkingDirectoryFd = preopened.currentWorkingDirectory.fold(
                ifLeft = { -1 },
            ) { resource: LinuxDirectoryFdResource ->
                val fd = WASI_FIRST_PREOPEN_FD + preopened.preopenedDirectories.size
                fileDescriptors[fd] = resource
                fd
            }
        }

        fun resolveNativeDirectoryFd(
            directory: BaseDirectory,
        ): Either<BadFileDescriptor, NativeDirectoryFd> = when (directory) {
            CurrentWorkingDirectory -> CURRENT_WORKING_DIRECTORY.right()
            is DirectoryFd -> fsLock.withLock {
                val resource = fileDescriptors[directory.fd] as? LinuxDirectoryFdResource
                resource?.nativeFd?.right() ?: BadFileDescriptor("FD ${directory.fd} is not a directory").left()
            }
        }

        fun onFileDescriptorRemovedUnsafe(
            fdResource: FdResource,
        ) {
            openedDirectories.values.remove(fdResource)
        }
    }

    companion object {
        @Throws(IOException::class)
        fun create(
            stdio: StandardInputOutput,
            isRootAccessAllowed: Boolean,
            currentWorkingDirectory: String,
            preopenedDirectories: List<PreopenedDirectory>,
        ): LinuxFileSystemState {
            val directories = preopenDirectories(currentWorkingDirectory, preopenedDirectories)
                .getOrElse { openError ->
                    throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
                }

            return LinuxFileSystemState(
                stdio,
                isRootAccessAllowed,
                directories,
            )
        }
    }
}
