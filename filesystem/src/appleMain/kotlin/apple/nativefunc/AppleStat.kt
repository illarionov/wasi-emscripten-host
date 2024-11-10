/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.ext.followSymlinksAsAtSymlinkFlags
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.EOVERFLOW
import platform.posix.errno
import platform.posix.fstat
import platform.posix.fstatat
import platform.posix.stat
import platform.posix.timespec

internal expect fun stat.toStructStat(): StructStat

internal fun appleStat(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
    followSymlinks: Boolean,
): Either<StatError, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstatat(
        baseDirectoryFd.posixFd,
        path,
        statBuf.ptr,
        followSymlinksAsAtSymlinkFlags(followSymlinks),
    )
    return if (exitCode == 0) {
        statBuf.toStructStat().right()
    } else {
        errno.errnoToStatError().left()
    }
}

internal fun appleStatFd(
    fd: Int,
): Either<StatError, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstat(fd, statBuf.ptr)
    return if (exitCode == 0) {
        statBuf.toStructStat().right()
    } else {
        errno.errnoToStatFdError().left()
    }
}

private fun Int.errnoToStatError(): StatError = when (this) {
    EACCES -> AccessDenied("Search permission is denied")
    EBADF, EIO -> this.errnoToStatFdError()
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
    ENOENT -> NoEntry("Component of request path does not exist")
    ENOTDIR -> NotDirectory("Not a directory")
    else -> InvalidArgument("Error `$this`")
}

private fun Int.errnoToStatFdError(): StatError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EIO -> IoError("No memory")
    EOVERFLOW -> AccessDenied("number of block too large")
    else -> InvalidArgument("Error `$this`")
}

internal fun timespec.toStructTimespec(): StructTimespec = StructTimespec(
    seconds = this.tv_sec,
    nanoseconds = this.tv_nsec,
)
