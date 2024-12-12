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
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.ESRCH
import platform.posix.UTIME_OMIT
import platform.posix.errno
import platform.posix.futimens
import platform.posix.timespec
import platform.posix.utimensat

internal fun appleSetTimestamp(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    atimeNanoseconds: Long?,
    mtimeNanoseconds: Long?,
    followSymlinks: Boolean,
): Either<SetTimestampError, Unit> = memScoped {
    val timespec: CPointer<timespec> = allocArray(2)
    timespec[0].set(atimeNanoseconds)
    timespec[1].set(mtimeNanoseconds)

    val resultCode = utimensat(
        baseDirectoryFd.posixFd,
        path.kString,
        timespec,
        followSymlinksAsAtSymlinkFlags(followSymlinks),
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToSetTimestampError().left()
    }
}

internal fun appleSetTimestamp(
    fd: NativeDirectoryFd,
    atimeNanoseconds: Long?,
    mtimeNanoseconds: Long?,
): Either<SetTimestampError, Unit> {
    require(fd != NativeDirectoryFd.CURRENT_WORKING_DIRECTORY)
    return appleSetTimestamp(NativeFileFd(fd.raw), atimeNanoseconds, mtimeNanoseconds)
}

internal fun appleSetTimestamp(
    fd: NativeFileFd,
    atimeNanoseconds: Long?,
    mtimeNanoseconds: Long?,
): Either<SetTimestampError, Unit> = memScoped {
    val timespec: CPointer<timespec> = allocArray(2)
    timespec[0].set(atimeNanoseconds)
    timespec[1].set(mtimeNanoseconds)

    val resultCode = futimens(fd.fd, timespec)
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToSetTimestampError().left()
    }
}

@Suppress("MagicNumber")
internal fun timespec.set(timeNanoseconds: Long?) {
    if (timeNanoseconds != null) {
        tv_sec = timeNanoseconds / 1_000_000_000L
        tv_nsec = timeNanoseconds % 1_000_000_000L
    } else {
        tv_sec = 0
        tv_nsec = UTIME_OMIT.toLong()
    }
}

private fun Int.errnoToSetTimestampError(): SetTimestampError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Invalid argument")
    EIO -> IoError("I/o error on readlink")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving")
    ENOENT -> NoEntry("Component of request path does not exist")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    EPERM -> PermissionDenied("Permission denied")
    EROFS -> ReadOnlyFileSystem("Read-only file system")
    ESRCH -> PermissionDenied("Search permission denied")
    else -> InvalidArgument("Error `$this`")
}
