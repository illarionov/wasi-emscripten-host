/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

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
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
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

internal object CheckAccessMapper {
    fun fileAccessibilityCheckToPosixModeFlags(
        flags: Set<FileAccessibilityCheck>,
    ): Int {
        if (flags.isEmpty()) {
            return F_OK
        }
        var mask = 0
        if (flags.contains(READABLE)) {
            mask = mask or R_OK
        }
        if (flags.contains(WRITEABLE)) {
            mask = mask or W_OK
        }
        if (flags.contains(EXECUTABLE)) {
            mask = mask or X_OK
        }
        return mask
    }

    fun checkAccessErrnoToCheckAccessError(errno: Int): CheckAccessError = when (errno) {
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
}
