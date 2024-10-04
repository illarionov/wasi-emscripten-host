/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoLock
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.AdvisorylockLockType
import at.released.weh.filesystem.op.lock.AdvisorylockLockType.READ
import at.released.weh.filesystem.op.lock.AdvisorylockLockType.WRITE
import at.released.weh.filesystem.posix.NativeFd
import at.released.weh.filesystem.posix.ext.toPosixWhence
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
import platform.posix.F_UNLCK
import platform.posix.F_WRLCK
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.flock

internal fun linuxAddAdvisoryLockFd(
    fd: NativeFd,
    flock: Advisorylock,
): Either<AdvisoryLockError, Unit> = memScoped {
    val structFlockInstance: flock = alloc<flock> {
        setFromAdvisoryLock(flock)
    }
    val exitCode = fcntl(fd.fd, F_SETLK, structFlockInstance)
    return if (exitCode == 0) {
        Unit.right()
    } else {
        errno.errnoToAdvisoryLockError(flock).left()
    }
}

internal fun linuxRemoveAdvisoryLock(
    fd: NativeFd,
    flock: Advisorylock,
): Either<AdvisoryLockError, Unit> = memScoped {
    val structFlockInstance: flock = alloc<flock> {
        setFromAdvisoryLock(flock)
        l_type = F_UNLCK.toShort()
    }
    val exitCode = fcntl(
        fd.fd,
        F_SETLK,
        structFlockInstance,
    )
    return if (exitCode == 0) {
        Unit.right()
    } else {
        errno.errnoToAdvisoryLockError(flock).left()
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

internal fun Int.errnoToAdvisoryLockError(
    lock: Advisorylock,
): AdvisoryLockError = when (this) {
    EACCES, EAGAIN -> Again("Can not lock `$lock`, operation prohibited`")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINTR -> Interrupted("Locking interrupted by signal")
    EINVAL -> InvalidArgument("Can not lock `$lock`, invalid argument")
    ENOLCK -> NoLock("Can not lock `$lock`, too many locks open")
    else -> InvalidArgument("Can not lock `$lock`: unknown error `$this`")
}
