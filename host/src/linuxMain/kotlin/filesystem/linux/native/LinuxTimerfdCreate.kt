/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("Filename")

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.IO
import at.released.weh.filesystem.model.FileSystemErrno.NFILE
import at.released.weh.filesystem.model.FileSystemErrno.PERM
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.poll.posixClockId
import at.released.weh.host.platform.linux.timerfd_create
import platform.posix.EINVAL
import platform.posix.EMFILE
import platform.posix.ENFILE
import platform.posix.EPERM
import platform.posix.errno

internal fun linuxTimerfdCreate(
    clock: SubscriptionClockId,
): Either<LinuxTimerfdCreateError, NativeFileFd> {
    val timerFd = timerfd_create(clock.posixClockId, 0)
    return if (timerFd != -1) {
        NativeFileFd(timerFd).right()
    } else {
        LinuxTimerfdCreateError().left()
    }
}

private fun LinuxTimerfdCreateError(error: Int = errno): LinuxTimerfdCreateError = when (error) {
    EINVAL -> LinuxTimerfdCreateError.InvalidArgument()
    EMFILE -> LinuxTimerfdCreateError.Nfile()
    ENFILE -> LinuxTimerfdCreateError.Nfile()
    EPERM -> LinuxTimerfdCreateError.PermissionDenied()
    else -> LinuxTimerfdCreateError.IoError("Other error. Errno: $error")
}

internal sealed class LinuxTimerfdCreateError : FileSystemOperationError {
    internal data class InvalidArgument(
        override val message: String = "Invalid argument",
    ) : LinuxTimerfdCreateError() {
        override val errno: FileSystemErrno = INVAL
    }

    internal data class Nfile(
        override val message: String = "Too many file descriptors open",
    ) : LinuxTimerfdCreateError() {
        override val errno: FileSystemErrno = NFILE
    }

    internal data class IoError(
        override val message: String,
    ) : LinuxTimerfdCreateError() {
        override val errno: FileSystemErrno = IO
    }

    internal data class PermissionDenied(
        override val message: String = "Requested clock is not available",
    ) : LinuxTimerfdCreateError() {
        override val errno: FileSystemErrno = PERM
    }
}
