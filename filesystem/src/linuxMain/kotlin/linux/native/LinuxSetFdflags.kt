/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.linux.ext.fdFdFlagsToLinuxMask
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EPERM
import platform.posix.F_SETFL
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.strerror

internal fun linuxSetFdflags(
    channel: NativeFileChannel,
    @FdflagsType fdFlags: Fdflags,
): Either<SetFdFlagsError, Unit> {
    val newFlags = fdFlags and (FdFlag.FD_APPEND.inv())
    return linuxSetFdflags(channel.fd, newFlags)
        .onRight {
            // XXX need be thread-safe
            channel.isInAppendMode = fdFlags and FdFlag.FD_APPEND == FdFlag.FD_APPEND
        }
}

internal fun linuxSetFdflags(
    fd: NativeFileFd,
    @FdflagsType fdFlags: Fdflags,
): Either<SetFdFlagsError, Unit> {
    val linuxFlags = fdFdFlagsToLinuxMask(fdFlags) and (FdFlag.FD_APPEND.toULong().inv())
    val exitCode = fcntl(fd.fd, F_SETFL, linuxFlags)
    return if (exitCode == 0) {
        Unit.right()
    } else {
        errno.errnoToSetFdFlagsError().left()
    }
}

private fun Int.errnoToSetFdFlagsError(): SetFdFlagsError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EPERM -> PermissionDenied("Can not clear the APPEND flag")
    else -> InvalidArgument("Other error: $this `${strerror(this)?.toKStringFromUtf8()}`")
}
