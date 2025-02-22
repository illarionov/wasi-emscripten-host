/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.memory

import arrow.core.Either
import at.released.weh.bindings.chicory.ext.trySetAccessibleCompat
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Ciovec
import at.released.weh.wasi.preview1.type.Iovec
import com.dylibso.chicory.runtime.ByteArrayMemory
import com.dylibso.chicory.runtime.ByteBufferMemory
import com.dylibso.chicory.runtime.Memory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.nio.ByteBuffer

@Suppress("ReturnCount", "SwallowedException")
internal fun tryCreateWasiMemoryReaderWriter(
    memory: Memory,
    fileSystem: FileSystem,
): ChicoryWasiMemoryOperations? {
    return try {
        val bufferField: Field = memory.javaClass.getDeclaredField("buffer")
        when {
            memory is ByteBufferMemory && ByteBuffer::class.java.isAssignableFrom(bufferField.type) ->
                ChicoryByteBufferWasiMemoryOperations(memory, fileSystem)

            memory is ByteArrayMemory && ByteArray::class.java.isAssignableFrom(bufferField.type) ->
                ChicoryByteArrayWasiMemoryOperations(memory, fileSystem)

            else -> null
        }
    } catch (nsfe: NoSuchFieldException) {
        null
    } catch (se: SecurityException) {
        null
    }
}

internal interface ChicoryWasiMemoryOperations : WasiMemoryWriter, WasiMemoryReader

private class ChicoryByteBufferWasiMemoryOperations(
    private val memory: ByteBufferMemory,
    private val fileSystem: FileSystem,
) : ChicoryWasiMemoryOperations {
    override fun read(fd: FileDescriptor, strategy: ReadWriteStrategy, iovecs: List<Iovec>): Either<ReadError, ULong> {
        val memoryBuffer = BYTE_BUFFER_MEMORY_BUFFER_HANDLE.invoke(memory) as ByteBuffer
        val bbufs = iovecs.map { iovec ->
            FileSystemByteBuffer(
                memoryBuffer.array(),
                memoryBuffer.arrayOffset() + iovec.buf,
                iovec.bufLen,
            )
        }
        return fileSystem.execute(ReadFd, ReadFd(fd, bbufs, strategy))
    }

    override fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong> {
        val memoryBuffer: ByteBuffer = BYTE_BUFFER_MEMORY_BUFFER_HANDLE.invoke(memory) as ByteBuffer
        val bbufs = cioVecs.map { ciovec ->
            FileSystemByteBuffer(
                memoryBuffer.array(),
                memoryBuffer.arrayOffset() + ciovec.buf,
                ciovec.bufLen,
            )
        }
        return fileSystem.execute(WriteFd, WriteFd(fd, bbufs, strategy))
    }

    companion object {
        @JvmStatic
        val BYTE_BUFFER_MEMORY_BUFFER_HANDLE: MethodHandle =
            ByteBufferMemory::class.java.getDeclaredField("buffer").let {
                it.trySetAccessibleCompat()
                MethodHandles.lookup().unreflectGetter(it)
            }
    }
}

private class ChicoryByteArrayWasiMemoryOperations(
    private val memory: ByteArrayMemory,
    private val fileSystem: FileSystem,
) : ChicoryWasiMemoryOperations {
    override fun read(fd: FileDescriptor, strategy: ReadWriteStrategy, iovecs: List<Iovec>): Either<ReadError, ULong> {
        val memoryArray = BYTE_ARRAY_MEMORY_BUFFER_HANDLE.invoke(memory) as ByteArray
        val bbufs = iovecs.map { iovec -> FileSystemByteBuffer(memoryArray, iovec.buf, iovec.bufLen) }
        return fileSystem.execute(ReadFd, ReadFd(fd, bbufs, strategy))
    }

    override fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong> {
        val memoryArray: ByteArray = BYTE_ARRAY_MEMORY_BUFFER_HANDLE.invoke(memory) as ByteArray
        val bbufs = cioVecs.map { ciovec -> FileSystemByteBuffer(memoryArray, ciovec.buf, ciovec.bufLen) }
        return fileSystem.execute(WriteFd, WriteFd(fd, bbufs, strategy))
    }

    companion object {
        @JvmStatic
        val BYTE_ARRAY_MEMORY_BUFFER_HANDLE: MethodHandle = ByteArrayMemory::class.java.getDeclaredField("buffer").let {
            it.trySetAccessibleCompat()
            MethodHandles.lookup().unreflectGetter(it)
        }
    }
}
