/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.AT_SYMLINK_NOFOLLOW
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.EOVERFLOW
import platform.posix.PATH_MAX
import platform.posix.errno
import platform.posix.fstatat
import platform.posix.readlinkat
import platform.posix.stat

private const val PATH_STEP = 1024
private val MAX_PATH_SIZE = maxOf(1024 * 1024, PATH_MAX)

@Suppress("ReturnCount")
internal fun appleReadLink(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
): Either<ReadLinkError, String> {
    var bufSize = getInitialBufSize(baseDirectoryFd, path)
        .getOrElse { return it.left() }
    do {
        val buf = ByteArray(bufSize)
        val bytesWritten = buf.usePinned {
            readlinkat(
                baseDirectoryFd.posixFd,
                path,
                it.addressOf(0),
                bufSize.toULong(),
            )
        }
        when {
            bytesWritten < 0 -> return errno.errnoToReadLinkError().left()
            bytesWritten < bufSize -> return buf.decodeToString(0, bytesWritten.toInt()).right()
            bufSize == MAX_PATH_SIZE -> return ENAMETOOLONG.errnoToReadLinkError().left()
            else -> bufSize = (bufSize + PATH_STEP).coerceAtMost(MAX_PATH_SIZE)
        }
    } while (true)
}

private fun getInitialBufSize(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
): Either<ReadLinkError, Int> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstatat(
        baseDirectoryFd.posixFd,
        path,
        statBuf.ptr,
        AT_SYMLINK_NOFOLLOW,
    )
    if (exitCode < 0) {
        return errno.errnoToReadLinkError().left()
    }

    return statBuf.st_size.let {
        if (it != 0L) {
            it.toInt() + 1
        } else {
            PATH_MAX
        }
    }.right()
}

private fun Int.errnoToReadLinkError(): ReadLinkError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Invalid argument in request")
    EIO -> IoError("I/o error on readlink")
    ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
    ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
    ENOENT -> NoEntry("Component of request path does not exist")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    EOVERFLOW -> IoError("File too big")
    else -> InvalidArgument("Error `$this`")
}
