/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.ext.asByteBuffer
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import java.io.IOException
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

internal fun NioFileChannel.nioRead(
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
) = when (strategy) {
    CHANGE_POSITION -> readChangePosition(iovecs)
    DO_NOT_CHANGE_POSITION -> readDoNotChangePosition(iovecs)
}

private fun NioFileChannel.readDoNotChangePosition(
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> = either {
    var position = this@readDoNotChangePosition.getPosition()
        .mapLeft(::toReadError)
        .bind()

    var totalBytesRead: ULong = 0U
    for (iovec in iovecs) {
        val byteBuffer = iovec.asByteBuffer()
        val bytesRead = readCatching {
            channel.read(byteBuffer, position)
        }.bind()
        if (bytesRead > 0) {
            position += bytesRead
            totalBytesRead += bytesRead.toULong()
        }
        if (bytesRead < byteBuffer.limit()) {
            break
        }
    }
    totalBytesRead
}

private fun NioFileChannel.readChangePosition(
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    val byteBuffers = Array(iovecs.size) { iovecs[it].asByteBuffer() }
    val bytesRead: Either<ReadError, Long> = readCatching {
        channel.read(byteBuffers)
    }
    return bytesRead.map {
        if (it != -1L) it.toULong() else 0UL
    }
}

@InternalWasiEmscriptenHostApi
public inline fun <R : Any> readCatching(
    block: () -> R,
): Either<ReadError, R> = Either.catch {
    block()
}.mapLeft {
    when (it) {
        is ClosedByInterruptException -> IoError("Interrupted")
        is AsynchronousCloseException -> IoError("Channel closed on other thread")
        is ClosedChannelException -> Interrupted("Channel closed")
        is NonReadableChannelException -> BadFileDescriptor("Non readable channel")
        is IOException -> IoError("I/O error: ${it.message}")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}

private fun toReadError(error: ChannelPositionError): ReadError = when (error) {
    is ChannelPositionError.ClosedChannel -> IoError(error.message)
    is ChannelPositionError.InvalidArgument -> InvalidArgument(error.message)
    is ChannelPositionError.IoError -> IoError(error.message)
}
