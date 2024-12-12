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
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.linux.mkdirat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EINVAL
import platform.posix.ELOOP
import platform.posix.EMLINK
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOMEM
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.errno

internal fun linuxMkdir(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    mode: Int,
    failIfExists: Boolean,
): Either<MkdirError, Unit> {
    val resultCode = mkdirat(
        baseDirectoryFd.linuxFd,
        path.kString,
        mode.toUInt(),
    )
    return when {
        resultCode == 0 -> Unit.right()
        !failIfExists && errno == EEXIST -> Unit.right()
        else -> errno.errnoToMkdirError().left()
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.errnoToMkdirError(): MkdirError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EEXIST -> Exists("Path with this name exists")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDQUOT -> DiskQuota("Disk quota exhausted.")
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symlinks")
    EMLINK -> Mlink("Too many links to the parent directory")
    ENAMETOOLONG -> NameTooLong("Name too long")
    ENOENT -> NoEntry("Component of path does not exist")
    ENOMEM -> IoError("No memory")
    ENOSPC -> NoSpace("No space for the new directory")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    EPERM -> AccessDenied("No permission to create directory.")
    EROFS -> ReadOnlyFileSystem(
        "Write permission requested for a file on a read-only filesystem.",
    )

    else -> InvalidArgument("Error `$this`")
}
