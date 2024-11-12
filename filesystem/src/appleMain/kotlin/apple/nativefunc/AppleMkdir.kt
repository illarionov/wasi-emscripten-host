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
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EILSEQ
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ELOOP
import platform.posix.EMLINK
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.mkdirat

internal fun appleMkdir(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
    mode: Int,
    failIfExists: Boolean,
): Either<MkdirError, Unit> {
    val resultCode = mkdirat(baseDirectoryFd.posixFd, path, mode.toUShort())
    return when {
        resultCode == 0 -> Unit.right()
        !failIfExists && errno == EEXIST -> Unit.right()
        else -> errno.errnoToMkdirError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errnoToMkdirError(): MkdirError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDQUOT -> DiskQuota("Disk quota exhausted.")
    EEXIST -> Exists("Path with this name exists")
    EILSEQ -> InvalidArgument("Invalid encoding")
    EIO -> IoError("I/o error")
    EISDIR -> InvalidArgument("The named file is a root directory")
    ELOOP -> TooManySymbolicLinks("Too many symlinks")
    EMLINK -> Mlink("Too many links to the parent directory")
    ENAMETOOLONG -> NameTooLong("Name too long")
    ENOENT -> NoEntry("Component of path does not exist")
    ENOSPC -> NoSpace("No space for the new directory")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    EROFS -> ReadOnlyFileSystem("Write permission requested for a file on a read-only filesystem.")
    else -> InvalidArgument("Error `$this`")
}
