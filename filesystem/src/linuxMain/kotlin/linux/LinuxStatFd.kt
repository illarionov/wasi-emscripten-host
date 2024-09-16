/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.stat.StructStat
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.ENOMEM

internal expect fun platformFstatFd(fd: Int): Either<Int, StructStat>

internal object LinuxStatFd : FileSystemOperationHandler<StatFd, StatError, StructStat> {
    override fun invoke(input: StatFd): Either<StatError, StructStat> {
        return platformFstatFd(input.fd).mapLeft {
            it.errnoToStatFdError(input)
        }
    }

    private fun Int.errnoToStatFdError(request: StatFd): StatError = when (this) {
        EACCES -> AccessDenied("Access denied for `$request`")
        EBADF -> BadFileDescriptor("Bad file descriptor ${request.fd}")
        ENOMEM -> IoError("No memory")
        else -> InvalidArgument("Error `$this`")
    }
}
