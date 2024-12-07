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
import at.released.weh.filesystem.error.Busy
import at.released.weh.filesystem.error.DirectoryNotEmpty
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.platform.linux.renameat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EBUSY
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EINVAL
import platform.posix.EISDIR
import platform.posix.ELOOP
import platform.posix.EMLINK
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.ENOTEMPTY
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.EXDEV
import platform.posix.errno
import platform.posix.strerror

internal fun linuxRename(
    oldPathFd: NativeDirectoryFd,
    oldPath: RealPath,
    newPathFd: NativeDirectoryFd,
    newPath: RealPath,
): Either<RenameError, Unit> {
    val resultCode = renameat(
        oldPathFd.linuxFd,
        oldPath,
        newPathFd.linuxFd,
        newPath,
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToRenameError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errnoToRenameError(): RenameError = when (this) {
    EACCES -> AccessDenied("Access to oldpath or newpath is denied")
    EBUSY -> Busy("Oldpath or newpath is in use")
    EDQUOT -> DiskQuota("User quota on filesystem resources has been exhausted")
    EINVAL -> InvalidArgument("Invalid flags")
    EISDIR -> PathIsDirectory("newpath is existing directory but oldpath is not a directory")
    ELOOP -> TooManySymbolicLinks("Too many symbolic links")
    EMLINK -> Mlink("oldpath or newpath has the maximum number of links")
    ENAMETOOLONG -> NameTooLong("oldpath or newpath too long")
    ENOENT -> NoEntry("Oldpath or newpath does not exist")
    ENOSPC -> NoSpace("Can not rename: no enough space")
    ENOTDIR -> NotDirectory("Oldpath or newpath is not a directory")
    ENOTEMPTY, EEXIST -> DirectoryNotEmpty("Newpath is a nonempty directory")
    EPERM -> AccessDenied("Access denied")
    EROFS -> ReadOnlyFileSystem("Write permission requested for a file on a read-only filesystem.")
    EXDEV -> IoError("Oldpath and newpath are not on the same mounted filesystem")
    EBADF -> BadFileDescriptor("oldpathfd or newpathfd is not a valid file descriptor")
    else -> IoError("Other error: $this (${strerror(this)?.toKStringFromUtf8()})")
}
