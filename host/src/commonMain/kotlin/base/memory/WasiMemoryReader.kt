/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.wasi.preview1.type.Iovec
import kotlinx.io.buffered

public fun interface WasiMemoryReader {
    public fun read(
        @Fd fd: Int,
        strategy: ReadWriteStrategy,
        iovecs: List<Iovec>,
    ): Either<ReadError, ULong>
}

public class DefaultWasiMemoryReader(
    private val memory: Memory,
    private val fileSystem: FileSystem,
) : WasiMemoryReader {
    override fun read(@Fd fd: Int, strategy: ReadWriteStrategy, iovecs: List<Iovec>): Either<ReadError, ULong> {
        val bbufs: List<FileSystemByteBuffer> = iovecs.createBuffers()
        return fileSystem.execute(ReadFd, ReadFd(fd, bbufs, strategy)).onRight { readBytes ->
            writeBuffersToMemory(bbufs, iovecs, readBytes)
        }
    }

    private fun writeBuffersToMemory(
        buffers: List<FileSystemByteBuffer>,
        dstAddresses: List<Iovec>,
        maxBytes: ULong,
    ) {
        var bytesLeft = maxBytes.toLong()
        for (vecNo in dstAddresses.indices) {
            if (bytesLeft == 0L) {
                break
            }
            @IntWasmPtr(Byte::class)
            val dstAddress: WasmPtr = dstAddresses[vecNo].buf
            val bbuf = buffers[vecNo]
            val size = minOf(bbuf.length, bytesLeft.toInt())

            // XXX: too many memory copies
            memory.sinkWithMaxSize(
                fromAddr = dstAddress,
                maxSize = size,
            ).buffered().use {
                it.write(bbuf.array, bbuf.offset, bbuf.offset + size)
            }
            bytesLeft -= size
        }
    }

    private fun List<Iovec>.createBuffers(): List<FileSystemByteBuffer> = map { iovec ->
        FileSystemByteBuffer(ByteArray(iovec.bufLen.value.toInt()))
    }
}
