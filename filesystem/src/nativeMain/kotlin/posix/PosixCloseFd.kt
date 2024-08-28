/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.posix.base.PosixFileSystemState
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.EIO
import platform.posix.ENOSPC
import platform.posix.close
import platform.posix.errno

internal expect fun Int.platformSpecificErrnoToCloseError(fd: Fd): CloseError

internal class PosixCloseFd(
    private val fsState: PosixFileSystemState,
) : FileSystemOperationHandler<CloseFd, CloseError, Unit> {
    override fun invoke(input: CloseFd): Either<CloseError, Unit> {
        fsState.remove(input.fd)
        val retval = close(input.fd.fd)
        return if (retval == 0) {
            Unit.right()
        } else {
            errno.errnoToCloseError(input.fd).left()
        }
    }

    private fun Int.errnoToCloseError(fd: Fd): CloseError = when (this) {
        EBADF -> BadFileDescriptor("Bad file descriptor $fd")
        EINTR -> Interrupted("Closing $fd interrupted by signal")
        EIO -> IoError("I/O error while closing $fd")
        ENOSPC -> NoSpace("No space to close $fd")
        else -> platformSpecificErrnoToCloseError(fd)
    }
}
