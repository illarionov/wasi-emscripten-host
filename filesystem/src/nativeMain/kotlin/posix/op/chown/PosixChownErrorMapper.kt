/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.chown

import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TooManySymbolicLinks
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

internal object PosixChownErrorMapper {
    fun errnoToChownFdError(
        errno: Int,
    ): ChownError = errnoToChownError(errno)

    fun errnoToChownError(
        errno: Int,
    ): ChownError = when (errno) {
        EACCES -> AccessDenied("Access denied")
        EBADF -> BadFileDescriptor("Bad file descriptor")
        EINVAL -> InvalidArgument("Invalid argument")
        EIO -> IoError("I/O error")
        ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving request")
        ENAMETOOLONG -> NameTooLong("Name too long while resolving request")
        ENOENT -> NoEntry("Component of request path does not exist")
        ENOMEM -> IoError("No memory")
        ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
        EPERM -> AccessDenied("File is immutable or append-only.")
        EROFS -> ReadOnlyFileSystem(
            "Write permission requested for a file on a read-only filesystem.",
        )

        else -> InvalidArgument("Error `$this`")
    }
}
