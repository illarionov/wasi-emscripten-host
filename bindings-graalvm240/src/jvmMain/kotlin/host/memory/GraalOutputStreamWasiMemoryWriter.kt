/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.memory

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.ext.writeCatching
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.nio.op.RunWithChannelFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.host.base.memory.DefaultWasiMemoryWriter
import at.released.weh.host.base.memory.WasiMemoryWriter
import at.released.weh.host.wasi.preview1.type.CiovecArray
import java.nio.channels.Channels
import java.nio.channels.FileChannel

internal class GraalOutputStreamWasiMemoryWriter(
    private val memory: GraalvmWasmHostMemoryAdapter,
    private val fileSystem: FileSystem,
    logger: Logger,
) : WasiMemoryWriter {
    private val logger = logger.withTag("FS:GrWriter")
    private val wasmMemory = memory.wasmMemory
    private val defaultMemoryWriter = DefaultWasiMemoryWriter(memory, fileSystem)

    override fun write(@Fd fd: Int, strategy: ReadWriteStrategy, cioVecs: CiovecArray): Either<WriteError, ULong> {
        return if (strategy == CHANGE_POSITION && fileSystem.isOperationSupported(RunWithChannelFd)) {
            val op = RunWithChannelFd(
                fd = fd,
                block = { writeChangePosition(it, cioVecs) },
            )
            fileSystem.execute(RunWithChannelFd.key(), op)
                .mapLeft { it as WriteError }
        } else {
            defaultMemoryWriter.write(fd, strategy, cioVecs)
        }
    }

    private fun writeChangePosition(
        channelResult: Either<BadFileDescriptor, FileChannel>,
        cioVecs: CiovecArray,
    ): Either<WriteError, ULong> {
        logger.v { "writeChangePosition($channelResult, ${cioVecs.ciovecList.map { it.bufLen.value }})" }
        val channel = channelResult.mapLeft {
            BadFileDescriptor(it.message)
        }.getOrElse {
            return it.left()
        }

        return writeCatching {
            var totalBytesWritten: ULong = 0U
            val outputStream = Channels.newOutputStream(channel).buffered()
            for (vec in cioVecs.ciovecList) {
                val limit = vec.bufLen.value.toInt()
                wasmMemory.copyToStream(memory.node, outputStream, vec.buf.addr, limit)
                totalBytesWritten += limit.toUInt()
            }
            outputStream.flush()
            totalBytesWritten
        }
    }
}
