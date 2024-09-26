/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.memory

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.wasi.preview1.type.Ciovec
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.sourceWithMaxSize
import kotlinx.io.buffered
import kotlinx.io.readByteArray

public fun interface WasiMemoryWriter {
    public fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong>
}

public class DefaultWasiMemoryWriter(
    private val memory: ReadOnlyMemory,
    private val fileSystem: FileSystem,
) : WasiMemoryWriter {
    override fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong> {
        val bufs = cioVecs.toByteBuffers(memory)
        return fileSystem.execute(WriteFd, WriteFd(fd, bufs, strategy))
    }

    private fun List<Ciovec>.toByteBuffers(
        memory: ReadOnlyMemory,
    ): List<FileSystemByteBuffer> = map { ciovec ->
        // XXX: too many memory copies
        val maxSize = ciovec.bufLen
        val bytesBuffer = memory.sourceWithMaxSize(ciovec.buf, maxSize).buffered().use {
            it.readByteArray(maxSize)
        }
        FileSystemByteBuffer(bytesBuffer)
    }
}
