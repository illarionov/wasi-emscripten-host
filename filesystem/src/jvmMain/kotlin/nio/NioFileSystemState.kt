/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.fdresource.BatchDirectoryOpener
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.fdresource.NioFdResource
import at.released.weh.filesystem.fdresource.NioFileFdResource
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.toFileDescriptorMap
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.nio.cwd.JvmPathResolver
import at.released.weh.filesystem.nio.cwd.PathResolver
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.preopened.VirtualPath
import at.released.weh.filesystem.stdio.StandardInputOutput
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import java.nio.file.FileSystem as NioFileSystem

internal class NioFileSystemState private constructor(
    val isRootAccessAllowed: Boolean,
    preopenedDescriptors: Map<FileDescriptor, FdResource>,
    val currentDirectoryFd: FileDescriptor,
    val javaFs: NioFileSystem = FileSystems.getDefault(),
) : AutoCloseable {
    val fdsLock: Lock = ReentrantLock()
    private val fds: FileDescriptorTable<FdResource> = FileDescriptorTable(preopenedDescriptors)
    val pathResolver: PathResolver = JvmPathResolver(javaFs, this)

    fun <E : FileSystemOperationError> addFile(
        path: Path,
        fdflags: Fdflags,
        rights: FdRightsBlock,
        channelFactory: (FileDescriptor) -> Either<E, FileChannel>,
    ): Either<E, Pair<FileDescriptor, NioFileFdResource>> = fdsLock.withLock {
        fds.allocate { fd: FileDescriptor ->
            channelFactory(fd).map {
                NioFileFdResource(
                    path = path,
                    channel = it,
                    fdflags = fdflags,
                    rights = rights,
                )
            }
        }
    }

    fun <E : FileSystemOperationError> addDirectory(
        virtualPath: VirtualPath,
        rights: FdRightsBlock,
        directoryFactory: (FileDescriptor) -> Either<E, Path>,
    ): Either<E, Pair<FileDescriptor, NioDirectoryFdResource>> = fdsLock.withLock {
        fds.allocate { fd: FileDescriptor ->
            directoryFactory(fd).map { realPath ->
                NioDirectoryFdResource(
                    realPath = realPath,
                    virtualPath = virtualPath,
                    isPreopened = false,
                    rights = rights,
                )
            }
        }
    }

    fun remove(
        @IntFileDescriptor fd: FileDescriptor,
    ): Either<BadFileDescriptor, FdResource> = fdsLock.withLock {
        return fds.release(fd)
    }

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = fdsLock.withLock {
        fds[fd]
    }

    fun findUnsafe(
        path: Path,
    ): NioFdResource? = fds.firstOrNull { it is NioFdResource && it.path == path }?.let { it as NioFdResource }

    inline fun <E : FileSystemOperationError, R : Any> executeWithResource(
        fd: FileDescriptor,
        crossinline block: (fdResource: FdResource) -> Either<E, R>,
    ): Either<E, R> {
        @Suppress("UNCHECKED_CAST")
        val resource: FdResource = get(fd) ?: return (BadFileDescriptor(fileDescriptorNotOpenMessage(fd)) as E).left()
        return block(resource)
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithPath(
        baseDirectory: BaseDirectory,
        relativePath: VirtualPath,
        followSymlinks: Boolean = false,
        crossinline block: (path: Either<ResolvePathError, Path>) -> Either<E, R>,
    ): Either<E, R> {
        val path = pathResolver.resolve(relativePath, baseDirectory, true, followSymlinks)
        return block(path)
    }

    override fun close() {
        val resources = fdsLock.withLock {
            fds.drain()
        }
        for (resource in resources) {
            try {
                resource.close()
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") ex: Exception) {
                // close(${chan.path}) failed. Ignore.
            }
        }
    }

    companion object {
        @Throws(IOException::class)
        fun create(
            stdio: StandardInputOutput,
            isRootAccessAllowed: Boolean,
            currentWorkingDirectory: String,
            preopenedDirectories: List<PreopenedDirectory>,
            javaFs: NioFileSystem = FileSystems.getDefault(),
        ): NioFileSystemState {
            val preopened = BatchDirectoryOpener(javaFs).preopen(currentWorkingDirectory, preopenedDirectories)
                .getOrElse { openError ->
                    throw IOException("Can not preopen `${openError.directory}`: ${openError.error}")
                }

            val preopenedMap: MutableMap<FileDescriptor, FdResource> = stdio.toFileDescriptorMap().toMutableMap()
            preopened.preopenedDirectories.entries.forEachIndexed { index, (_: RealPath, resource: FdResource) ->
                preopenedMap[index + WASI_FIRST_PREOPEN_FD] = resource
            }

            val currentWorkingDirectoryFd = preopened.currentWorkingDirectory.fold(
                ifLeft = { -1 },
            ) { resource: NioDirectoryFdResource ->
                val fd = WASI_FIRST_PREOPEN_FD + preopened.preopenedDirectories.size
                preopenedMap[fd] = resource
                fd
            }

            return NioFileSystemState(isRootAccessAllowed, preopenedMap, currentWorkingDirectoryFd, javaFs)
        }
    }
}
