/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.open.fdFdFlagsToPosixMask
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EPERM
import platform.posix.F_SETFL
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.strerror

internal fun appleSetFdflags(
    channel: NativeFileChannel,
    @FdflagsType fdFlags: Fdflags,
): Either<SetFdFlagsError, Unit> {
    return appleSetFdflags(channel.fd, fdFlags)
}

internal fun appleSetFdflags(
    fd: NativeFileFd,
    @FdflagsType fdFlags: Fdflags,
): Either<SetFdFlagsError, Unit> {
    val newFlags = fdFdFlagsToPosixMask(fdFlags)
    val exitCode = fcntl(fd.fd, F_SETFL, newFlags)
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
