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
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.platform.linux.fchmodat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOMEM
import platform.posix.ENOTDIR
import platform.posix.ENOTSUP
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.fchmod

internal fun linuxChmod(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @FileMode mode: Int,
    followSymlinks: Boolean,
): Either<ChmodError, Unit> {
    val resultCode = fchmodat(
        baseDirectoryFd.linuxFd,
        path.kString,
        mode.toUInt(),
        getChmodFlags(followSymlinks),
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToChmodError().left()
    }
}

internal fun linuxChmodFd(
    fd: NativeDirectoryFd,
    @FileMode mode: Int,
): Either<ChmodError, Unit> {
    require(fd != NativeDirectoryFd.CURRENT_WORKING_DIRECTORY)
    val resultCode = fchmod(fd.raw, mode.toUInt())
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToChmodFdError().left()
    }
}

internal fun linuxChmodFd(
    nativeFd: NativeFileFd,
    @FileMode mode: Int,
): Either<ChmodError, Unit> {
    val resultCode = fchmod(nativeFd.fd, mode.toUInt())
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToChmodFdError().left()
    }
}

private fun getChmodFlags(
    followSymlinks: Boolean,
): Int = if (!followSymlinks) {
    AT_SYMLINK_NOFOLLOW
} else {
    0
}

private fun Int.errnoToChmodFdError(): ChmodError = errnoToChmodError()

private fun Int.errnoToChmodError(): ChmodError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Invalid argument in request")
    EIO -> IoError("I/O error")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
    ENOENT -> NoEntry("Component of request path does not exist")
    ENOMEM -> IoError("No memory")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    ENOTSUP -> NotSupported("Flag not supported.")
    EPERM -> AccessDenied("file is immutable or append-only.")
    EROFS -> ReadOnlyFileSystem(
        "Write permission requested for a file on a read-only filesystem.",
    )

    else -> InvalidArgument("Error `$this`")
}
