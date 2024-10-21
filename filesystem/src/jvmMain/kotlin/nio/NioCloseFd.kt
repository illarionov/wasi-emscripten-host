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
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.close.CloseFd
import kotlin.concurrent.withLock

internal class NioCloseFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<CloseFd, CloseError, Unit> {
    override fun invoke(input: CloseFd): Either<CloseError, Unit> = fsState.fsLock.withLock {
        val testFdResource = fsState.get(input.fd)
        if (testFdResource is NioDirectoryFdResource && testFdResource.isPreopened) {
            // Preopened directory is not closeable while working with the file system
            return BadFileDescriptor("Preopened directory is not closeable while working with the file system").left()
        }

        val fdResource = fsState.remove(input.fd)
            .mapLeft { BadFileDescriptor(it.message) }
            .getOrElse { badFileDescriptorError ->
                return badFileDescriptorError.left()
            }
        return fdResource.close()
    }
}
