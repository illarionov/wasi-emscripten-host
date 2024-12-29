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
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.host.platform.linux.timerfd_settime
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.linux.TFD_TIMER_ABSTIME
import platform.posix.EBADF
import platform.posix.ECANCELED
import platform.posix.EINVAL
import platform.posix.errno
import platform.posix.itimerspec

internal fun linuxTimerfdSetTime(
    fd: NativeFileFd,
    timeoutNs: Long,
    isAbsolute: Boolean,
): Either<LinuxSettimeError, Unit> = memScoped {
    val itmeSpec: itimerspec = alloc<itimerspec>().apply {
        it_value.set(timeoutNs)
        it_interval.set(0)
    }
    val settimeFlags = if (isAbsolute) {
        TFD_TIMER_ABSTIME
    } else {
        0
    }
    val settimeResult = timerfd_settime(fd.fd, settimeFlags, itmeSpec.ptr, null)
    if (settimeResult == -1) {
        LinuxSettimeError().left()
    } else {
        Unit.right()
    }
}

private fun LinuxSettimeError(error: Int = errno): LinuxSettimeError = when (error) {
    EBADF -> LinuxSettimeError.BadFileDescriptor()
    EINVAL -> LinuxSettimeError.InvalidArgument()
    ECANCELED -> LinuxSettimeError.Canceled()
    else -> LinuxSettimeError.Other("Other error. Errno: $errno")
}

internal sealed class LinuxSettimeError : FileSystemOperationError {
    internal data class BadFileDescriptor(
        override val message: String = "File descriptor is not a timer",
    ) : LinuxSettimeError() {
        override val errno: FileSystemErrno = FileSystemErrno.BADF
    }

    internal data class InvalidArgument(
        override val message: String = "Invalid argument",
    ) : LinuxSettimeError() {
        override val errno: FileSystemErrno = FileSystemErrno.INVAL
    }

    internal data class Canceled(
        override val message: String = "Timer canceled",
    ) : LinuxSettimeError() {
        override val errno: FileSystemErrno = FileSystemErrno.CANCELED
    }

    internal data class Other(
        override val message: String,
    ) : LinuxSettimeError() {
        override val errno: FileSystemErrno = FileSystemErrno.IO
    }
}
