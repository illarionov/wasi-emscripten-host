/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.fdresource.NioFileFdResource
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.nio.cwd.JvmPathResolver
import at.released.weh.filesystem.nio.cwd.PathResolver
import at.released.weh.filesystem.stdio.StandardInputOutput
import java.nio.channels.FileChannel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import java.nio.file.FileSystem as NioFileSystem

internal class NioFileSystemState(
    val javaFs: NioFileSystem = FileSystems.getDefault(),
    val stdio: StandardInputOutput,
) : AutoCloseable {
    val fsLock: Lock = ReentrantLock()
    private val fdsLock: Lock = ReentrantLock()
    private val fds: FileDescriptorTable<FdResource> = FileDescriptorTable<FdResource>().apply {
        StdioFileFdResource.initStdioDescriptors(this, stdio)
    }
    val pathResolver: PathResolver = JvmPathResolver(javaFs, this)

    fun addFile(
        path: Path,
        channel: FileChannel,
    ): Either<Nfile, Pair<FileDescriptor, NioFileFdResource>> = fdsLock.withLock {
        fds.allocate { _ ->
            NioFileFdResource(
                fileSystem = this,
                path = path,
                channel = channel,
            )
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
        fun NioFileSystemState.getFileResource(
            @IntFileDescriptor fd: FileDescriptor,
        ): NioFileFdResource? = get(fd) as? NioFileFdResource
    }
}
