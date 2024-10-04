/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import kotlin.concurrent.withLock

internal class NioAddAdvisoryLockFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<AddAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: AddAdvisoryLockFd): Either<AdvisoryLockError, Unit> = fsState.fsLock.withLock {
        val channel: FdResource = fsState.get(input.fd)
            ?: return BadFileDescriptor("File descriptor ${input.fd} is not open or cannot be locked").left()
        return channel.addAdvisoryLock(input.flock)
    }
}
