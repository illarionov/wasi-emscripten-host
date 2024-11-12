/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.CPointer
import platform.posix.DIR
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EMFILE
import platform.posix.ENFILE
import platform.posix.ENOENT
import platform.posix.ENOMEM
import platform.posix.ENOTDIR
import platform.posix.errno
import platform.posix.fdopendir
import platform.posix.strerror

internal fun posixOpenDir(
    fd: NativeDirectoryFd,
): Either<ReadDirError, CPointer<DIR>> {
    val dir: CPointer<DIR>? = fdopendir(fd.raw)
    return dir?.right() ?: errno.errnoToReadDirError().left()
}

private fun Int.errnoToReadDirError(): ReadDirError = when (this) {
    EACCES -> AccessDenied("Access denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EMFILE -> Mfile("Too many open files")
    ENFILE -> Nfile("Too many open files")
    ENOENT -> NoEntry("Directory does not exist")
    ENOMEM -> IoError("No memory")
    ENOTDIR -> NotDirectory("Not a directory")
    else -> IoError("Error $this (${strerror(this)})")
}
