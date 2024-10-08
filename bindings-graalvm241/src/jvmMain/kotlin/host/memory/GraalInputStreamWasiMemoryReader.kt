/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.memory

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.fdresource.nio.readCatching
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.nio.op.RunWithChannelFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.type.Iovec
import java.nio.channels.Channels
import java.nio.channels.FileChannel

internal class GraalInputStreamWasiMemoryReader(
    private val memory: GraalvmWasmHostMemoryAdapter,
    private val fileSystem: FileSystem,
) : WasiMemoryReader {
    private val wasmMemory get() = memory.wasmMemory
    private val defaultMemoryReader = DefaultWasiMemoryReader(memory, fileSystem)

    override fun read(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        iovecs: List<Iovec>,
    ): Either<ReadError, ULong> {
        return if (strategy == CHANGE_POSITION && fileSystem.isOperationSupported(RunWithChannelFd)) {
            val op = RunWithChannelFd(
                fd = fd,
                block = { readChangePosition(it, iovecs) },
            )
            fileSystem.execute(RunWithChannelFd.key(), op)
                .mapLeft { it as ReadError }
        } else {
            defaultMemoryReader.read(fd, strategy, iovecs)
        }
    }

    private fun readChangePosition(
        channelResult: Either<BadFileDescriptor, FileChannel>,
        iovecs: List<Iovec>,
    ): Either<ReadError, ULong> {
        val channel = channelResult.mapLeft {
            BadFileDescriptor(it.message)
        }.getOrElse {
            return it.left()
        }
        return readCatching {
            var totalBytesRead: ULong = 0U
            val inputStream = Channels.newInputStream(channel).buffered()
            for (vec in iovecs) {
                val limit = vec.bufLen
                val bytesRead = wasmMemory.copyFromStream(memory.node, inputStream, vec.buf, limit)
                if (bytesRead > 0) {
                    totalBytesRead += bytesRead.toULong()
                }
                if (bytesRead < limit) {
                    break
                }
            }
            totalBytesRead
        }
    }
}
