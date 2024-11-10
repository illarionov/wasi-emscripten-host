/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.platformSpecificErrnoToCloseError
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.EIO
import platform.posix.ENOSPC
import platform.posix.errno

internal fun posixClose(
    fd: NativeDirectoryFd,
): Either<CloseError, Unit> {
    require(fd != NativeDirectoryFd.CURRENT_WORKING_DIRECTORY)
    return posixClose(NativeFileFd(fd.raw))
}

internal fun posixClose(
    nativeFd: NativeFileFd,
): Either<CloseError, Unit> {
    val retval = platform.posix.close(nativeFd.fd)
    return if (retval == 0) {
        Unit.right()
    } else {
        errno.errnoToCloseError(nativeFd).left()
    }
}

private fun Int.errnoToCloseError(fd: NativeFileFd): CloseError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor $fd")
    EINTR -> Interrupted("Closing $fd interrupted by signal")
    EIO -> IoError("I/O error while closing $fd")
    ENOSPC -> NoSpace("No space to close $fd")
    else -> platformSpecificErrnoToCloseError(fd.fd)
}
