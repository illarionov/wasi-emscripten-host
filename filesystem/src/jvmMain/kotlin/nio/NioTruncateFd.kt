/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenedMessage
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.wasi.filesystem.common.Fd
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

internal class NioTruncateFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<TruncateFd, TruncateError, Unit> {
    override fun invoke(input: TruncateFd): Either<TruncateError, Unit> {
        val channel = fsState.fileDescriptors.get(input.fd)
            ?: return BadFileDescriptor(fileDescriptorNotOpenedMessage(input.fd)).left()
        return Either.catch {
            channel.channel.truncate(input.length)
            // TODO: extend file size to length?
            Unit
        }.mapLeft { error ->
            error.toTruncateError(input.fd)
        }
    }

    private fun Throwable.toTruncateError(@Fd fd: Int): TruncateError = when (this) {
        is NonReadableChannelException -> InvalidArgument("Read-only channel")
        is ClosedChannelException -> BadFileDescriptor(fileDescriptorNotOpenedMessage(fd))
        is IllegalArgumentException -> InvalidArgument("Negative length")
        is IOException -> IoError("I/O Error: $message")
        else -> throw IllegalStateException("Unexpected error", this)
    }
}
