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
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
import at.released.weh.filesystem.platform.linux.AT_EACCESS
import at.released.weh.filesystem.platform.linux.AT_EMPTY_PATH
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.platform.linux.SYS_faccessat2
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOMEM
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.ETXTBSY
import platform.posix.F_OK
import platform.posix.R_OK
import platform.posix.W_OK
import platform.posix.X_OK
import platform.posix.errno
import platform.posix.syscall

internal fun linuxCheckAccess(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean = false,
    allowEmptyPath: Boolean = false,
    followSymlinks: Boolean = false,
): Either<CheckAccessError, Unit> =
    linuxCheckAccess(baseDirectoryFd.linuxFd, path, mode, useEffectiveUserId, allowEmptyPath, followSymlinks)

private fun linuxCheckAccess(
    nativeFdOrArCwd: Int,
    path: String,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean = false,
    allowEmptyPath: Boolean = false,
    followSymlinks: Boolean = false,
): Either<CheckAccessError, Unit> {
    val resultCode = syscall(
        SYS_faccessat2.toLong(),
        nativeFdOrArCwd,
        path,
        mode.toModeFlags(),
        getCheckAccessFlags(useEffectiveUserId, allowEmptyPath, followSymlinks),
    )
    return if (resultCode == 0L) {
        Unit.right()
    } else {
        errno.errnoToCheckAccessError().left()
    }
}

private fun getCheckAccessFlags(
    useEffectiveUserId: Boolean = false,
    allowEmptyPath: Boolean = false,
    followSymlinks: Boolean = false,
): Int {
    var mask = 0
    if (useEffectiveUserId) {
        mask = mask and AT_EACCESS
    }
    if (allowEmptyPath) {
        mask = mask and AT_EMPTY_PATH
    }
    if (!followSymlinks) {
        mask = mask and AT_SYMLINK_NOFOLLOW
    }
    return mask
}

private fun Set<FileAccessibilityCheck>.toModeFlags(): Int {
    if (this.isEmpty()) {
        return F_OK
    }
    var mask = 0
    if (this.contains(READABLE)) {
        mask = mask and R_OK
    }
    if (this.contains(WRITEABLE)) {
        mask = mask and W_OK
    }
    if (this.contains(EXECUTABLE)) {
        mask = mask and X_OK
    }
    return mask
}

private fun Int.errnoToCheckAccessError(): CheckAccessError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Invalid argument`")
    EIO -> IoError("I/O error")
    ELOOP -> TooManySymbolicLinks("Too many symlinks")
    ENAMETOOLONG -> NameTooLong("Name too long")
    ENOENT -> NoEntry("Component does not exist")
    ENOMEM -> IoError("No memory")
    ENOTDIR -> NotDirectory("Error while resolving path: not a directory")
    EPERM -> AccessDenied("Write permission requested to a file with immutable flag.")
    EROFS -> ReadOnlyFileSystem("Write permission requested for a file on a read-only filesystem.")
    ETXTBSY -> TextFileBusy("Write permission requested to executed file.")
    else -> InvalidArgument("Unexpected error `$this`")
}
