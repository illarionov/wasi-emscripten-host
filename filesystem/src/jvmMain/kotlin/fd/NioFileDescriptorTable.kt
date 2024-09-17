/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fd

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.nio.NioFileSystemState
import at.released.weh.wasi.filesystem.common.Fd
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class NioFileDescriptorTable(
    private val fsState: NioFileSystemState,
) : AutoCloseable {
    private val fds: FileDescriptorTable<NioFileHandle> = FileDescriptorTable()
    private val lock: Lock = ReentrantLock()

    fun add(
        path: Path,
        channel: FileChannel,
    ): Either<Nfile, NioFileHandle> = lock.withLock {
        fds.allocate { fd ->
            NioFileHandle(
                fileSystem = fsState,
                fd = fd,
                path = path,
                channel = channel,
            )
        }
    }

    fun remove(@Fd fd: Int): Either<BadFileDescriptor, NioFileHandle> = lock.withLock {
        return fds.release(fd)
    }

    fun get(
        @Fd fd: Int,
    ): NioFileHandle? = lock.withLock {
        fds[fd]
    }

    override fun close() {
        val channels = lock.withLock {
             fds.drain()
        }
        for (chan in channels) {
            try {
                chan.channel.close()
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") ex: Exception) {
                // close(${chan.path}) failed. Ignore.
            }
        }
    }
}
