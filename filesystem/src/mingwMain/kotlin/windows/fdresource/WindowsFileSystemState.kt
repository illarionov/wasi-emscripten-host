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
import at.released.weh.filesystem.error.NotDirectory
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
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.fdresource.WindowsFileFdResource.WindowsFileChannel
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.IOException

internal class WindowsFileSystemState private constructor(
    stdio: StandardInputOutput,
    internal val isRootAccessAllowed: Boolean,
    preopenedDirectories: PreopenedDirectories,
) : AutoCloseable {
    private val fdsLock: ReentrantLock = reentrantLock()
    private val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable(stdio.toFileDescriptorMap())
    val pathResolver = PathResolver(fileDescriptors, fdsLock)

    init {
        pathResolver.setupPreopenedDirectories(preopenedDirectories)
    }

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

    // TODO: resolve with absolute path
    inline fun <E : FileSystemOperationError, R : Any> executeWithBaseDirectoryResource(
        baseDirectory: BaseDirectory,
        crossinline block: (handle: WindowsDirectoryChannel?) -> Either<E, R>,
    ): Either<E, R> {
        val nativeFd: WindowsDirectoryChannel? = pathResolver.resolveNativeDirectory(baseDirectory)
            .getOrElse { bfe: FileSystemOperationError ->
                @Suppress("UNCHECKED_CAST")
                return (bfe as E).left()
            }
        return block(nativeFd)
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

            preopened
                .preopenedDirectories
                .entries
                .forEachIndexed { index, (path: RealPath, ch: WindowsDirectoryChannel) ->
                    val resource = WindowsDirectoryFdResource(ch)
                    fileDescriptors[index + WASI_FIRST_PREOPEN_FD] = resource
                    openedDirectories[path] = resource
                }

            currentWorkingDirectoryFd = preopened.currentWorkingDirectory.fold(
                ifLeft = { -1 },
            ) { channel: WindowsDirectoryChannel ->
                val fd = WASI_FIRST_PREOPEN_FD + preopened.preopenedDirectories.size
                fileDescriptors[fd] = WindowsDirectoryFdResource(channel)
                fd
            }
        }

        fun resolveNativeDirectory(
            directory: BaseDirectory,
        ): Either<FileSystemOperationError, WindowsDirectoryChannel?> = when (directory) {
            CurrentWorkingDirectory -> if (currentWorkingDirectoryFd != INVALID_FD) {
                getDirectoryNativeFd(currentWorkingDirectoryFd)
            } else {
                null.right()
            }

            is DirectoryFd -> getDirectoryNativeFd(directory.fd)
        }

        private fun getDirectoryNativeFd(
            fd: FileDescriptor,
        ): Either<FileSystemOperationError, WindowsDirectoryChannel> = fsLock.withLock {
            when (val fdResource = fileDescriptors[fd]) {
                null -> BadFileDescriptor("Directory File descriptor $fd is not open").left()
                is WindowsDirectoryFdResource -> fdResource.channel.right()
                else -> NotDirectory("FD $fd is not a directory").left()
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
        ): WindowsFileSystemState {
            val directories = preopenDirectories(currentWorkingDirectory, preopenedDirectories)
                .getOrElse { openError ->
                    throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
                }

            return WindowsFileSystemState(
                stdio,
                isRootAccessAllowed,
                directories,
            )
        }
    }
}