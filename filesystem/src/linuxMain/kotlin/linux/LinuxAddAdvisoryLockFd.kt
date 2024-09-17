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
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoLock
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.AdvisorylockLockType
import at.released.weh.filesystem.op.lock.AdvisorylockLockType.READ
import at.released.weh.filesystem.op.lock.AdvisorylockLockType.WRITE
import at.released.weh.filesystem.posix.ext.toPosixWhence
import at.released.weh.wasi.filesystem.common.Fd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.EACCES
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENOLCK
import platform.posix.F_RDLCK
import platform.posix.F_SETLK
import platform.posix.F_WRLCK
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.flock

internal object LinuxAddAdvisoryLockFd : FileSystemOperationHandler<AddAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: AddAdvisoryLockFd): Either<AdvisoryLockError, Unit> = memScoped {
        val structFlockInstance: flock = alloc<flock> {
            setFromAdvisoryLock(input.flock)
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

    internal fun flock.setFromAdvisoryLock(lock: Advisorylock) {
        l_type = lock.type.toFlockType()
        l_whence = lock.whence.toPosixWhence().toShort()
        l_start = lock.start
        l_len = lock.length
        l_pid = 0
    }

    internal fun AdvisorylockLockType.toFlockType(): Short = when (this) {
        READ -> F_RDLCK
        WRITE -> F_WRLCK
    }.toShort()

    internal fun Int.errnoToAdvisoryLockError(@Fd fd: Int, lock: Advisorylock): AdvisoryLockError = when (this) {
        EACCES, EAGAIN -> Again("Can not lock `$fd - $lock`, operation prohibited`")
        EBADF -> BadFileDescriptor("Bad file descriptor $fd")
        EINTR -> Interrupted("Locking $fd interrupted by signal")
        EINVAL -> InvalidArgument("Can not lock `$fd - $lock`, invalid argument")
        ENOLCK -> NoLock("Can not lock `$fd - $lock`, too many locks open")
        else -> InvalidArgument("Can not lock `$fd - $lock`: unknown error `$this`")
    }
}
