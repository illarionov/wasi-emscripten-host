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
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.linux.AT_EMPTY_PATH
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_FOLLOW
import at.released.weh.filesystem.platform.linux.linkat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.EXDEV
import platform.posix.errno
import platform.posix.strerror

internal fun linuxHardlink(
    oldBaseDirectoryFd: NativeDirectoryFd,
    oldPath: PosixRealPath,
    newBaseDirectoryFd: NativeDirectoryFd,
    newPath: PosixRealPath,
    followSymlinks: Boolean = false,
    allowEmptyPath: Boolean = false,
): Either<HardlinkError, Unit> {
    var flags = 0
    if (allowEmptyPath) {
        flags = flags or AT_EMPTY_PATH
    }
    if (followSymlinks) {
        flags = flags or AT_SYMLINK_FOLLOW
    }

    // TODO: check RESOLVE_BENEATH
    val resultCode = linkat(
        oldBaseDirectoryFd.linuxFd,
        oldPath.kString,
        newBaseDirectoryFd.linuxFd,
        newPath.kString,
        flags,
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToLinkError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errnoToLinkError(): HardlinkError = when (this) {
    EACCES -> AccessDenied("Access to linkpath denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDQUOT -> DiskQuota("User quota on filesystem resources has been exhausted")
    EEXIST -> Exists("Linkpath exists")
    EINVAL -> InvalidArgument("Invalid flags specified")
    EIO -> IoError("I/O error")
    ELOOP -> TooManySymbolicLinks("Too many symbolic links")
    ENAMETOOLONG -> NameTooLong("Linkpath name too long")
    ENOENT -> NoEntry("Old path or new path does not exist")
    ENOSPC -> NoSpace("Can not symlink: no enough space")
    ENOTDIR -> NotDirectory("Error while resolving path: not a directory")
    EPERM -> PermissionDenied("Old path is a directory or no permission to create hardlink")
    EROFS -> ReadOnlyFileSystem("Write permission requested for a file on a read-only filesystem.")
    EXDEV -> PermissionDenied("Oldpath and newpath are not on the same mounted filesystem")
    else -> IoError("Other error: $this (${strerror(this)?.toKStringFromUtf8()})")
}
