/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.sync.SyncFd
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ENOSPC
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.fdatasync
import platform.posix.fsync

internal object LinuxSync : FileSystemOperationHandler<SyncFd, SyncError, Unit> {
    override fun invoke(input: SyncFd): Either<SyncError, Unit> {
        val resultCode = if (input.syncMetadata) {
            fsync(input.fd.fd)
        } else {
            fdatasync(input.fd.fd)
        }

        return if (resultCode == 0) {
            Unit.right()
        } else {
            errno.errNoToSyncError(input).left()
        }
    }

    private fun Int.errNoToSyncError(request: SyncFd): SyncError = when (this) {
        EBADF -> BadFileDescriptor("Bad file descriptor `${request.fd}`")
        EINTR -> Interrupted("Sync interrupted by signal")
        EIO -> IoError("I/o error on sync")
        ENOSPC -> NoSpace("Can not sync: no enough space")
        EROFS, EINVAL -> InvalidArgument("Sync not supported")
        EDQUOT -> NoSpace("Can not sync: no space")
        else -> IoError("Other error. Errno: $errno")
    }
}
