/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import arrow.core.Either
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.WriteError
import java.io.IOException
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException

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
