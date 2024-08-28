/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

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
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.ext.toDirFd
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
import at.released.weh.filesystem.platform.linux.AT_EACCESS
import at.released.weh.filesystem.platform.linux.AT_EMPTY_PATH
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.platform.linux.SYS_faccessat2
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

internal object LinuxCheckAccess : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> {
        val resultCode = syscall(
            SYS_faccessat2.toLong(),
            input.baseDirectory.toDirFd(),
            input.path,
            input.mode.toModeFlags(),
            input.toCheckAccessFlags(),
        )
        return if (resultCode == 0L) {
            Unit.right()
        } else {
            errno.errnoToCheckAccessError(input).left()
        }
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

    private fun CheckAccess.toCheckAccessFlags(): Int {
        var mask = 0
        if (this.useEffectiveUserId) {
            mask = mask and AT_EACCESS
        }
        if (this.allowEmptyPath) {
            mask = mask and AT_EMPTY_PATH
        }
        if (!this.followSymlinks) {
            mask = mask and AT_SYMLINK_NOFOLLOW
        }
        return mask
    }

    private fun Int.errnoToCheckAccessError(request: CheckAccess): CheckAccessError = when (this) {
        EACCES -> AccessDenied("Access denied for `$request`")
        EBADF -> BadFileDescriptor("Bad file descriptor ${request.baseDirectory}")
        EINVAL -> InvalidArgument("Invalid argument in `$request`")
        EIO -> IoError("I/O error")
        ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving `$request`")
        ENAMETOOLONG -> NameTooLong("Name too long while resolving `$request`")
        ENOENT -> NoEntry("Component of `${request.path}` does not exist")
        ENOMEM -> IoError("No memory")
        ENOTDIR -> NotDirectory("Error while resolving `${request.path}`: not a directory")
        EPERM -> AccessDenied("Write permission requested to a file with immutable flag. Request: `$request`")
        EROFS -> ReadOnlyFileSystem(
            "Write permission requested for a file on a read-only filesystem. Request: `$request`",
        )

        ETXTBSY -> TextFileBusy("Write permission requested to executed file. Request: $request")
        else -> InvalidArgument("Unexpected error `$this`")
    }
}
