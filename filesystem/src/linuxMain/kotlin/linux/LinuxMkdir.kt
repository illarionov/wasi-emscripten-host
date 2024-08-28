/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
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
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.ext.toDirFd
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.platform.linux.mkdirat
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
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

internal object LinuxMkdir : FileSystemOperationHandler<Mkdir, MkdirError, Unit> {
    override fun invoke(input: Mkdir): Either<MkdirError, Unit> {
        val resultCode = mkdirat(
            input.baseDirectory.toDirFd(),
            input.path,
            input.mode.mask,
        )
        return if (resultCode == 0) {
            Unit.right()
        } else {
            errno.errnoToMkdirError(input).left()
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun Int.errnoToMkdirError(request: Mkdir): MkdirError = when (this) {
        EACCES -> AccessDenied("Access denied for `$request`")
        EBADF -> BadFileDescriptor("Bad file descriptor ${request.baseDirectory}")
        EDQUOT -> DiskQuota("Disk quota exhausted. Request: $request")
        EINVAL -> InvalidArgument("Invalid argument in `$request`")
        ELOOP -> TooManySymbolicLinks("Too many symlinks while resolving `$request`")
        EMLINK -> Mlink("Too many links to the parent directory")
        ENAMETOOLONG -> NameTooLong("Name too long while resolving `$request`")
        ENOENT -> NoEntry("Component of `${request.path}` does not exist")
        ENOMEM -> IoError("No memory")
        ENOSPC -> NoSpace("No space for the new directory")
        ENOTDIR -> NotDirectory("Error while resolving `${request.path}`: not a directory")
        EPERM -> AccessDenied("No permission to create directory. Request: `$request`")
        EROFS -> ReadOnlyFileSystem(
            "Write permission requested for a file on a read-only filesystem. Request: `$request`",
        )

        else -> InvalidArgument("Error `$this`")
    }
}
