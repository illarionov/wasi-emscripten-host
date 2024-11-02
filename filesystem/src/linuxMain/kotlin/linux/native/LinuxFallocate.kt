/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FileTooBig
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENODEV
import platform.posix.ENOSPC
import platform.posix.ENOTSUP
import platform.posix.ESPIPE
import platform.posix.posix_fallocate
import platform.posix.strerror

internal fun posixFallocate(
    fd: NativeFileFd,
    offset: Long,
    len: Long,
): Either<FallocateError, Unit> {
    val resultCode = posix_fallocate(fd.fd, offset, len)
    return if (resultCode == 0) {
        Either.Right(Unit)
    } else {
        Either.Left(fallocateCodeToFallocateError(resultCode))
    }
}

private fun fallocateCodeToFallocateError(code: Int): FallocateError = when (code) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EFBIG -> FileTooBig("File size too big")
    EINTR -> Interrupted("Fallocate interrupted by signal")
    EINVAL -> InvalidArgument("Incorrect offset or length")
    ENODEV -> BadFileDescriptor("Not a file")
    ENOSPC -> NoSpace("Can not pre-allocate: no space")
    ENOTSUP -> NotSupported("Fallocate is not not supported by file system")
    ESPIPE -> BadFileDescriptor("Can not fallocate on a pipe")
    else -> InvalidArgument("Other error $code (${strerror(code)?.toKStringFromUtf8()})")
}
