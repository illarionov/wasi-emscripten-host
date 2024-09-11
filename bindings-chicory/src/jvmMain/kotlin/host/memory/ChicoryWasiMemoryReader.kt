/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.memory

import arrow.core.Either
import at.released.weh.bindings.chicory.ext.isJvmOrAndroidMinApi34
import at.released.weh.bindings.chicory.ext.trySetAccessibleCompat
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.base.memory.DefaultWasiMemoryReader
import at.released.weh.host.base.memory.WasiMemoryReader
import at.released.weh.host.wasi.preview1.type.IovecArray
import com.dylibso.chicory.runtime.Memory
import java.lang.reflect.Field
import java.nio.ByteBuffer

internal class ChicoryWasiMemoryReader(
    private val memory: Memory,
    private val fileSystem: FileSystem,
    private val bufferField: Field,
) : WasiMemoryReader {
    override fun read(fd: Fd, strategy: ReadWriteStrategy, iovecs: IovecArray): Either<ReadError, ULong> {
        val memoryByteBuffer = bufferField.get(memory) as? ByteBuffer
            ?: error("Can not get memory byte buffer")
        check(memoryByteBuffer.hasArray()) { "MemoryBuffer without array" }
        val bbufs = iovecs.toByteBuffers(memoryByteBuffer)
        return fileSystem.execute(ReadFd, ReadFd(fd, bbufs, strategy))
    }

    private fun IovecArray.toByteBuffers(
        memoryBuffer: ByteBuffer,
    ): List<FileSystemByteBuffer> = List(iovecList.size) { iovecNo ->
        val ioVec = iovecList[iovecNo]
        FileSystemByteBuffer(
            memoryBuffer.array(),
            memoryBuffer.arrayOffset() + ioVec.buf.addr,
            ioVec.bufLen.value.toInt(),
        )
    }

    companion object {
        fun createOrDefault(
            memory: ChicoryMemoryAdapter,
            fileSystem: FileSystem,
        ): WasiMemoryReader = if (isJvmOrAndroidMinApi34()) {
            tryCreate(memory.wasmMemory, fileSystem)
        } else {
            null
        } ?: DefaultWasiMemoryReader(memory, fileSystem)

        @Suppress("ReturnCount", "SwallowedException")
        fun tryCreate(
            memory: Memory,
            fileSystem: FileSystem,
        ): ChicoryWasiMemoryReader? {
            try {
                val bufferField: Field = Memory::class.java.getDeclaredField("buffer")
                if (!bufferField.trySetAccessibleCompat()) {
                    return null
                }
                if (bufferField.get(memory) !is ByteBuffer) {
                    return null
                }
                return ChicoryWasiMemoryReader(memory, fileSystem, bufferField)
            } catch (nsfe: NoSuchFieldException) {
                return null
            } catch (se: SecurityException) {
                return null
            }
        }
    }
}