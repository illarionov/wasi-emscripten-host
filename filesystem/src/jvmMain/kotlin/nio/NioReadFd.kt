/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.op.readwrite.ReadFd
import kotlin.concurrent.withLock

internal class NioReadFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadFd, ReadError, ULong> {
    override fun invoke(input: ReadFd): Either<ReadError, ULong> = fsState.fsLock.withLock {
        val channel = fsState.get(input.fd)
            ?: return BadFileDescriptor(fileDescriptorNotOpenMessage(input.fd)).left()
        return channel.read(input.iovecs, input.strategy)
    }
}
