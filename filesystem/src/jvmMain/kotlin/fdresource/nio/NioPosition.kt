/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.fdresource.nio.ChannelPositionError.ClosedChannel
import at.released.weh.filesystem.model.Whence
import java.io.IOException
import java.nio.channels.ClosedChannelException

internal fun NioFileChannel.getPosition(): Either<ChannelPositionError, Long> = Either.catch {
    channel.position()
}.mapLeft {
    when (it) {
        is ClosedChannelException -> ClosedChannel("Channel `$path` closed (${it.message})")
        is IOException -> ChannelPositionError.IoError("I/O error: ${it.message}")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}

@InternalWasiEmscriptenHostApi
public fun NioFileChannel.setPosition(
    fileDelta: Long,
    whence: Whence = Whence.SET,
): Either<ChannelPositionError, Long> = resolveWhencePosition(fileDelta, whence)
    .flatMap { newPosition ->
        if (newPosition >= 0) {
            setPosition(newPosition)
        } else {
            ChannelPositionError.InvalidArgument("Incorrect new position: $newPosition").left()
        }
    }

private fun NioFileChannel.setPosition(
    newPosition: Long,
): Either<ChannelPositionError, Long> = Either.catch {
    channel.position(newPosition)
    newPosition
}.mapLeft {
    when (it) {
        is ClosedChannelException -> ClosedChannel("Channel `$channel` closed (${it.message})")
        is IllegalArgumentException -> ChannelPositionError.InvalidArgument("Negative new position (${it.message})")
        is IOException -> ChannelPositionError.IoError("I/O error: ${it.message}")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}

@InternalWasiEmscriptenHostApi
public fun NioFileChannel.resolveWhencePosition(
    offset: Long,
    whence: Whence,
): Either<ChannelPositionError, Long> = when (whence) {
    Whence.SET -> offset.right()
    Whence.CUR -> this.getPosition().map { it + offset }
    Whence.END -> Either.catch {
        channel.size() - offset
    }.mapLeft {
        when (it) {
            is ClosedChannelException -> ClosedChannel("Channel `$path` closed (${it.message})")
            is IOException -> ChannelPositionError.IoError("I/O error: ${it.message}")
            else -> throw IllegalStateException("Unexpected error", it)
        }
    }
}
