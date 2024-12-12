/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd

internal class LinuxRemoveAdvisoryLockFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<RemoveAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: RemoveAdvisoryLockFd): Either<AdvisoryLockError, Unit> =
        fsState.executeWithResource(input.fd) {
            it.removeAdvisoryLock(input.flock)
        }
}
