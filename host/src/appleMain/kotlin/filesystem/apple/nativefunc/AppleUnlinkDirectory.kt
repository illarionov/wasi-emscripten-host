/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Busy
import at.released.weh.filesystem.error.DirectoryNotEmpty
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.AT_REMOVEDIR
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EBUSY
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.ENOTEMPTY
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.unlinkat

internal fun appleUnlinkDirectory(
    directoryFd: NativeDirectoryFd,
    path: PosixRealPath,
): Either<UnlinkError, Unit> {
    val resultCode = unlinkat(directoryFd.posixFd, path.kString, AT_REMOVEDIR)
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToUnlinkDirectoryError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errnoToUnlinkDirectoryError(): UnlinkError = when (this) {
    EACCES -> AccessDenied("Cannot unlink directory, access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EBUSY -> Busy("Cannot delete directory because it is being used by another process.")
    EINVAL -> InvalidArgument("Invalid flag value specified in unlinkat()")
    EIO -> IoError("Cannot delete directory: I/O error.`")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
    ENOENT -> NoEntry("Component of request path does not exist or empty")
    ENOTDIR -> NotDirectory("Path is not a directory")
    ENOTEMPTY -> DirectoryNotEmpty("Directory not empty")
    EPERM -> PermissionDenied("Can not delete directory: permission denied")
    EROFS -> InvalidArgument("Can node delete directory: read-only file system")
    else -> InvalidArgument("Other error. Errno: $this")
}
