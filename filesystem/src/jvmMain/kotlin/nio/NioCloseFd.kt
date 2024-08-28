/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.close.CloseFd
import java.io.IOException
import kotlin.concurrent.withLock

internal class NioCloseFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<CloseFd, CloseError, Unit> {
    override fun invoke(input: CloseFd): Either<CloseError, Unit> = fsState.fsLock.withLock {
        val fileChannel = fsState.fileDescriptors.remove(input.fd)
            .mapLeft { BadFileDescriptor(it.message) }
            .getOrElse { badFileDescriptorError ->
                return badFileDescriptorError.left()
            }
        return Either.catch {
            fileChannel.channel.close()
        }.mapLeft {
            when (it) {
                is IOException -> IoError("I/O error: ${it.message}")
                else -> throw IllegalStateException("Unexpected error", it)
            }
        }
    }
}
