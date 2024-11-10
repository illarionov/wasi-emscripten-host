/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.posix.NativeFileFd
import platform.posix.EBADF
import platform.posix.EDEADLK
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.ftruncate

internal fun appleTruncate(
    fd: NativeFileFd,
    length: Long,
): Either<TruncateError, Unit> {
    val resultCode = ftruncate(fd.fd, length)
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.appleErrNoToTruncateError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.appleErrNoToTruncateError(): TruncateError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDEADLK -> AccessDenied("Can not materialize dataless file")
    EFBIG -> InvalidArgument("File too big")
    EINTR -> IoError("Truncate interrupted by signal")
    EINVAL -> InvalidArgument("Invalid argument")
    EIO -> IoError("I/o error on truncate")
    EROFS -> InvalidArgument("File on read-only file system")
    else -> IoError("Other error. Errno: $errno")
}
