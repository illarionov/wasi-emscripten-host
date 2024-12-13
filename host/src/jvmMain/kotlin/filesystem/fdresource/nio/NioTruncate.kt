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
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

internal fun NioFileChannel.truncate(length: Long): Either<TruncateError, Unit> = Either.catch {
    // XXX this operation should be atomic
    val appendBytes = length - this.channel.size()
    if (appendBytes < 0) {
        val filePosition = channel.position()
        channel.truncate(length)
        if (filePosition > length) {
            channel.position(filePosition)
        }
    } else if (appendBytes > 0) {
        extendPreTouch(appendBytes)
    }
    Unit
}.mapLeft { error ->
    toTruncateError(error)
}

private fun NioFileChannel.toTruncateError(error: Throwable): TruncateError =
    when (error) {
        is NonReadableChannelException -> InvalidArgument("Read-only channel")
        is ClosedChannelException -> BadFileDescriptor("File `$path` is not open")
        is IllegalArgumentException -> InvalidArgument("Negative length")
        is IOException -> IoError("I/O Error: ${error.message}")
        else -> throw IllegalStateException("Unexpected error", error)
    }
