/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.memory

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

/**
 * Interface to implement optimized reading of large data blocks from the file system to the WASM virtual memory.
 *
 * The `read` function reads data from the file descriptor `fd` into WASM memory at addresses specified in the `iovecs`.
 *
 * Implementations may leverage direct access to the source memory to avoid unnecessary data copying
 * into intermediate buffers whenever possible.
 */
public fun interface WasiMemoryReader {
    public fun read(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        iovecs: List<Iovec>,
    ): Either<ReadError, ULong>
}

public class DefaultWasiMemoryReader(
    private val memory: Memory,
    private val fileSystem: FileSystem,
) : WasiMemoryReader {
    override fun read(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        iovecs: List<Iovec>,
    ): Either<ReadError, ULong> {
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
        FileSystemByteBuffer(ByteArray(iovec.bufLen))
    }
}
