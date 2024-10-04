/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.posix.NativeFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOSPC
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.ETXTBSY
import platform.posix.errno
import platform.posix.ftruncate

internal fun linuxTruncate(
    fd: NativeFd,
    length: Long,
): Either<TruncateError, Unit> {
    val resultCode = ftruncate(fd.fd, length)
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errNoToTruncateError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errNoToTruncateError(): TruncateError = when (this) {
    EACCES -> AccessDenied("No access")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDQUOT -> NoSpace("Can not truncate file: no space")
    EINTR -> IoError("Truncate interrupted by signal")
    EINVAL -> InvalidArgument("Invalid argument")
    EIO -> IoError("I/o error on truncate")
    EISDIR -> PathIsDirectory("File descriptor is a directory")
    ELOOP -> TooManySymbolicLinks("Too many symlinks")
    ENAMETOOLONG -> NameTooLong("Name too long")
    ENOENT -> NoEntry("Component of path does not exist")
    ENOSPC -> NoSpace("Can not truncate file: no enough space")
    EPERM -> AccessDenied("No permission to truncate file")
    EROFS -> InvalidArgument("File on read-only file system")
    ETXTBSY -> TextFileBusy("Can not truncate: file is executing")
    else -> IoError("Other error. Errno: $errno")
}
