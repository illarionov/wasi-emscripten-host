/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.op.readwrite.WriteFd
import kotlin.concurrent.withLock

internal class NioWriteFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<WriteFd, WriteError, ULong> {
    override fun invoke(input: WriteFd): Either<WriteError, ULong> = fsState.fsLock.withLock {
        val channel = fsState.get(input.fd)
            ?: return BadFileDescriptor(fileDescriptorNotOpenMessage(input.fd)).left()
        return channel.write(input.cIovecs, input.strategy)
    }
}
