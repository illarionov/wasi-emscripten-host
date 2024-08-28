/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import arrow.core.Either
import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.wasi.preview1.type.Iovec
import at.released.weh.host.wasi.preview1.type.IovecArray
import kotlinx.io.buffered

public fun interface WasiMemoryReader {
    public fun read(
        fd: Fd,
        strategy: ReadWriteStrategy,
        iovecs: IovecArray,
    ): Either<ReadError, ULong>
}

public class DefaultWasiMemoryReader(
    private val memory: Memory,
    private val fileSystem: FileSystem,
    logger: Logger,
) : WasiMemoryReader {
    private val logger: Logger = logger.withTag("DefaultWasiMemoryReader")

    override fun read(fd: Fd, strategy: ReadWriteStrategy, iovecs: IovecArray): Either<ReadError, ULong> {
        logger.v { "read($fd, ${iovecs.iovecList.map { it.bufLen.value }})" }
        val bbufs: List<FileSystemByteBuffer> = iovecs.createBuffers()

        return fileSystem.execute(ReadFd, ReadFd(fd, bbufs, strategy)).onRight { readBytes ->
            var bytesLeft = readBytes.toLong()
            for (vecNo in iovecs.iovecList.indices) {
                if (bytesLeft == 0L) {
                    break
                }
                val vec: Iovec = iovecs.iovecList[vecNo]
                val bbuf = bbufs[vecNo]
                val size = minOf(bbuf.length, bytesLeft.toInt())

                // XXX: too many memory copies
                memory.sinkWithMaxSize(vec.buf, size).buffered().use {
                    it.write(bbuf.array, bbuf.offset, bbuf.offset + size)
                }
                bytesLeft -= size
            }
        }
    }

    private fun IovecArray.createBuffers(): List<FileSystemByteBuffer> = List(iovecList.size) {
        FileSystemByteBuffer(ByteArray(iovecList[it].bufLen.value.toInt()))
    }
}
