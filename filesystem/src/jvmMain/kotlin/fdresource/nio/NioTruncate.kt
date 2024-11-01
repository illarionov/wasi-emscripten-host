/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.TruncateError
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

internal fun NioFileChannel.truncate(length: Long): Either<TruncateError, Unit> = Either.catch {
    // XXX this operation should be atomic
    val appendBytes = length - this.channel.size()
    if (appendBytes < 0) {
        channel.truncate(length)
    } else if (appendBytes > 0) {
        extend(appendBytes)
    }
    Unit
}.mapLeft { error ->
    toTruncateError(error)
}

internal const val MAX_BUF_SIZE = 8192L

private fun NioFileChannel.extend(appendBytes: Long) {
    val startPosition = channel.size()
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
        writeZeroBytesOrThrow(buffer, lastBlockSize, lastBlockPosition)
    }

    // Explicitly fill the new space with zeros, as the default value of new bytes is undefined.
    if (fullBlocks > 0) {
        writeZeroBytesOrThrow(buffer, fullBlocks * MAX_BUF_SIZE, startPosition)
    }
}

private fun NioFileChannel.writeZeroBytesOrThrow(
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

private fun NioFileChannel.toTruncateError(error: Throwable): TruncateError =
    when (error) {
        is NonReadableChannelException -> InvalidArgument("Read-only channel")
        is ClosedChannelException -> BadFileDescriptor("File `$path` is not open")
        is IllegalArgumentException -> InvalidArgument("Negative length")
        is IOException -> IoError("I/O Error: ${error.message}")
        else -> throw IllegalStateException("Unexpected error", error)
    }
