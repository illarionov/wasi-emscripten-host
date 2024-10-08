/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.SyncError
import java.io.IOException
import java.nio.channels.ClosedChannelException

internal fun NioFileChannel.sync(syncMetadata: Boolean): Either<SyncError, Unit> {
    return Either.catch {
        channel.force(syncMetadata)
    }.mapLeft {
        when (it) {
            is ClosedChannelException -> BadFileDescriptor("File `$path` is not open")
            is IOException -> IoError("I/O error: ${it.message}")
            else -> throw IllegalStateException("Unexpected error", it)
        }
    }
}
