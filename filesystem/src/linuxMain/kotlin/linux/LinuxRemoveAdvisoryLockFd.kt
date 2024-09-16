/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.LinuxAddAdvisoryLockFd.errnoToAdvisoryLockError
import at.released.weh.filesystem.linux.LinuxAddAdvisoryLockFd.setFromAdvisoryLock
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.F_SETLK
import platform.posix.F_UNLCK
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.flock

internal object LinuxRemoveAdvisoryLockFd : FileSystemOperationHandler<RemoveAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: RemoveAdvisoryLockFd): Either<AdvisoryLockError, Unit> = memScoped {
        val structFlockInstance: flock = alloc<flock> {
            setFromAdvisoryLock(input.flock)
            l_type = F_UNLCK.toShort()
        }
        val exitCode = fcntl(
            input.fd,
            F_SETLK,
            structFlockInstance,
        )
        return if (exitCode == 0) {
            Unit.right()
        } else {
            errno.errnoToAdvisoryLockError(input.fd, input.flock).left()
        }
    }
}
