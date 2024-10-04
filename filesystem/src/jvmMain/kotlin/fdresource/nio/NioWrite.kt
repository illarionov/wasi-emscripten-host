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
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.ext.asByteBuffer
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import java.io.IOException
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonWritableChannelException

internal fun NioFileChannel.writeDoNotChangePosition(
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> = either {
    var position = getPosition()
        .mapLeft(::toWriteError)
        .bind()

    var totalBytesWritten = 0UL
    for (ciovec in cIovecs) {
        val byteBuffer = ciovec.asByteBuffer()
        val bytesWritten = writeCatching {
            channel.write(byteBuffer, position)
        }.bind()
        if (bytesWritten > 0) {
            position += bytesWritten
            totalBytesWritten += bytesWritten.toULong()
        }
        if (bytesWritten < byteBuffer.limit()) {
            break
        }
    }
    totalBytesWritten
}

internal fun NioFileChannel.writeChangePosition(cIovecs: List<FileSystemByteBuffer>): Either<WriteError, ULong> {
    val byteBuffers = Array(cIovecs.size) { cIovecs[it].asByteBuffer() }
    return writeCatching {
        channel.write(byteBuffers).toULong()
    }
}

@InternalWasiEmscriptenHostApi
public inline fun <R : Any> writeCatching(
    block: () -> R,
): Either<WriteError, R> = Either.catch {
    block()
}.mapLeft {
    when (it) {
        is ClosedByInterruptException -> IoError("Interrupted")
        is AsynchronousCloseException -> IoError("Channel closed on other thread")
        is ClosedChannelException -> Interrupted("Channel closed")
        is NonWritableChannelException -> BadFileDescriptor("Non writeable channel")
        is IOException -> IoError("I/O error: ${it.message}")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}

private fun toWriteError(error: ChannelPositionError): WriteError = when (error) {
    is ChannelPositionError.ClosedChannel -> IoError(error.message)
    is ChannelPositionError.InvalidArgument -> InvalidArgument(error.message)
    is ChannelPositionError.IoError -> IoError(error.message)
}
