/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

internal fun NioFileChannel.nioFallocate(
    offset: Long,
    length: Long,
): Either<FallocateError, Unit> {
    if (offset < 0) {
        return InvalidArgument("Incorrect offset $offset").left()
    }
    if (length <= 0) {
        return InvalidArgument("Incorrect length $length").left()
    }

    val startPosition: Long
    val appendBytes: Long

    val fileSize = this.channel.size()
    if (offset < fileSize) {
        startPosition = fileSize
        appendBytes = length - (fileSize - offset)
    } else {
        startPosition = offset
        appendBytes = length
    }
    return if (appendBytes > 0) {
        Either.catch {
            extendPreTouch(appendBytes, startPosition)
        }.mapLeft {
            fallocateThrowableToFallocateError(it)
        }
    } else {
        Unit.right()
    }
}

internal const val MAX_BUF_SIZE = 8192L

internal fun NioFileChannel.extendPreTouch(
    appendBytes: Long,
    startPosition: Long = channel.size(),
) {
    val buffer = ByteBuffer.allocate(appendBytes.coerceAtMost(MAX_BUF_SIZE).toInt())

    val fullBlocks: Long
    val lastBlockSize: Long
    if (appendBytes % MAX_BUF_SIZE != 0L) {
        fullBlocks = appendBytes / MAX_BUF_SIZE
        lastBlockSize = appendBytes % MAX_BUF_SIZE
    } else {
        fullBlocks = appendBytes / MAX_BUF_SIZE - 1
        lastBlockSize = MAX_BUF_SIZE
    }

    // Write last block to reserve full size
    run {
        val lastBlockPosition = startPosition + appendBytes - lastBlockSize
        fillWithZeroBytesOrThrow(buffer, lastBlockSize, lastBlockPosition)
    }

    // Explicitly fill the new space with zeros, as the default value of new bytes is undefined.
    if (fullBlocks > 0) {
        fillWithZeroBytesOrThrow(buffer, fullBlocks * MAX_BUF_SIZE, startPosition)
    }
}

private fun NioFileChannel.fillWithZeroBytesOrThrow(
    zeroBytesBufer: ByteBuffer,
    writeBytes: Long,
    startPosition: Long,
) {
    var position = startPosition
    val endPosition = startPosition + writeBytes
    while (position < endPosition) {
        val bytesToWrite = (endPosition - position).coerceAtMost(MAX_BUF_SIZE)
        zeroBytesBufer.position(0)
        zeroBytesBufer.limit(bytesToWrite.toInt())
        val bytesWritten = channel.write(zeroBytesBufer, position)
        position += bytesWritten
    }
}

private fun fallocateThrowableToFallocateError(error: Throwable): FallocateError = when (error) {
    is NonReadableChannelException -> InvalidArgument("Read-only channel")
    is ClosedChannelException -> BadFileDescriptor("File is not open")
    is IllegalArgumentException -> InvalidArgument("Negative length")
    is IOException -> IoError("I/O Error: ${error.message}")
    else -> throw IllegalStateException("Unexpected error", error)
}
