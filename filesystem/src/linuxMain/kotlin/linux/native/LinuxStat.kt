/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOMEM
import platform.posix.ENOTDIR

internal expect fun platformFstatat(
    dirfd: Int,
    path: PosixRealPath,
    statFlags: Int,
): Either<Int, StructStat>

internal expect fun platformFstatFd(fd: Int): Either<Int, StructStat>

internal fun linuxStat(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    followSymlinks: Boolean,
): Either<StatError, StructStat> {
    return platformFstatat(
        baseDirectoryFd.linuxFd,
        path,
        getStatFlags(followSymlinks),
    ).mapLeft {
        it.errnoToStatError()
    }
}

internal fun linuxStatFd(
    fd: Int,
): Either<StatError, StructStat> {
    return platformFstatFd(fd).mapLeft(Int::errnoToStatFdError)
}

private fun getStatFlags(followSymlinks: Boolean): Int = if (followSymlinks) {
    0
} else {
    AT_SYMLINK_NOFOLLOW
}

private fun Int.errnoToStatError(): StatError = when (this) {
    EACCES, EBADF, ENOMEM -> this.errnoToStatFdError()
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
    ENOENT -> NoEntry("Component of request path does not exist")
    ENOTDIR -> NotDirectory("Not a directory")
    else -> InvalidArgument("Error `$this`")
}

private fun Int.errnoToStatFdError(): StatError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    ENOMEM -> IoError("No memory")
    else -> InvalidArgument("Error `$this`")
}
