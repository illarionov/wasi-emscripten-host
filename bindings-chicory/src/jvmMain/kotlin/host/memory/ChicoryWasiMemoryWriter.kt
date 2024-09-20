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
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryWriter
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.CioVec
import com.dylibso.chicory.runtime.Memory
import java.lang.reflect.Field
import java.nio.ByteBuffer

internal class ChicoryWasiMemoryWriter private constructor(
    private val memory: Memory,
    private val fileSystem: FileSystem,
    private val bufferField: Field,
) : WasiMemoryWriter {
    override fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<CioVec>,
    ): Either<WriteError, ULong> {
        val memoryByteBuffer = bufferField.get(memory) as? ByteBuffer
            ?: error("Can not get memory byte buffer")
        val bbufs = cioVecs.toByteBuffers(memoryByteBuffer)
        return fileSystem.execute(WriteFd, WriteFd(fd, bbufs, strategy))
    }

    private fun List<CioVec>.toByteBuffers(
        memoryBuffer: ByteBuffer,
    ): List<FileSystemByteBuffer> = map { cioVec ->
        FileSystemByteBuffer(
            memoryBuffer.array(),
            memoryBuffer.arrayOffset() + cioVec.buf,
            cioVec.bufLen.value,
        )
    }

    companion object {
        fun createOrDefault(
            memory: ChicoryMemoryAdapter,
            fileSystem: FileSystem,
        ): WasiMemoryWriter = if (isJvmOrAndroidMinApi34()) {
            tryCreate(memory.wasmMemory, fileSystem)
        } else {
            null
        } ?: DefaultWasiMemoryWriter(memory, fileSystem)

        @Suppress("ReturnCount", "SwallowedException")
        fun tryCreate(
            memory: Memory,
            fileSystem: FileSystem,
        ): ChicoryWasiMemoryWriter? {
            try {
                val bufferField = Memory::class.java.getDeclaredField("buffer")
                if (!bufferField.trySetAccessibleCompat()) {
                    return null
                }
                if (bufferField.get(memory) !is ByteBuffer) {
                    return null
                }
                return ChicoryWasiMemoryWriter(memory, fileSystem, bufferField)
            } catch (nsfe: NoSuchFileException) {
                return null
            } catch (se: SecurityException) {
                return null
            }
        }
    }
}
