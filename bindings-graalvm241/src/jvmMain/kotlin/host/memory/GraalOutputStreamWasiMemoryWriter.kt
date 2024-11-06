/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.memory

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.fdresource.nio.ChannelPositionError
import at.released.weh.filesystem.fdresource.nio.NioFileChannel
import at.released.weh.filesystem.fdresource.nio.isInAppendMode
import at.released.weh.filesystem.fdresource.nio.setPosition
import at.released.weh.filesystem.fdresource.nio.writeCatching
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.model.Whence.END
import at.released.weh.filesystem.nio.op.RunWithChannelFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryWriter
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Ciovec
import java.nio.channels.Channels

internal class GraalOutputStreamWasiMemoryWriter(
    private val memory: GraalvmWasmHostMemoryAdapter,
    private val fileSystem: FileSystem,
    logger: Logger,
) : WasiMemoryWriter {
    private val logger = logger.withTag("FS:GrWriter")
    private val wasmMemory = memory.wasmMemory
    private val defaultMemoryWriter = DefaultWasiMemoryWriter(memory, fileSystem)

    override fun write(
        @IntFileDescriptor fd: FileDescriptor,
        strategy: ReadWriteStrategy,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong> {
        return if (strategy == CurrentPosition && fileSystem.isOperationSupported(RunWithChannelFd)) {
            val op = RunWithChannelFd(
                fd = fd,
                block = { writeChangePosition(it, cioVecs) },
                nonNioResourceFallback = { defaultMemoryWriter.write(fd, strategy, cioVecs) },
            )
            fileSystem.execute(RunWithChannelFd.key(), op)
                .mapLeft { it as WriteError }
        } else {
            defaultMemoryWriter.write(fd, strategy, cioVecs)
        }
    }

    private fun writeChangePosition(
        channelResult: Either<BadFileDescriptor, NioFileChannel>,
        cioVecs: List<Ciovec>,
    ): Either<WriteError, ULong> {
        logger.v { "writeChangePosition($channelResult, ${cioVecs.map(Ciovec::bufLen)})" }
        val channel = channelResult.mapLeft {
            BadFileDescriptor(it.message)
        }.getOrElse {
            return it.left()
        }
        if (channel.isInAppendMode()) {
            // XXX change position and write should be atomic
            channel.setPosition(0, END).mapLeft(::toWriteError)
                .onLeft { return it.left() }
        }

        return writeCatching {
            var totalBytesWritten: ULong = 0U
            val outputStream = Channels.newOutputStream(channel.channel).buffered()
            for (vec in cioVecs) {
                val limit = vec.bufLen
                wasmMemory.copyToStream(memory.node, outputStream, vec.buf, limit)
                totalBytesWritten += limit.toUInt()
            }
            outputStream.flush()
            totalBytesWritten
        }
    }

    private fun toWriteError(error: ChannelPositionError): WriteError = when (error) {
        is ChannelPositionError.ClosedChannel -> IoError(error.message)
        is ChannelPositionError.InvalidArgument -> InvalidArgument(error.message)
        is ChannelPositionError.IoError -> IoError(error.message)
    }
}
