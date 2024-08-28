/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.Overflow
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.posix.ext.errnoToSeekError
import at.released.weh.filesystem.posix.ext.toPosixWhence
import platform.posix.errno
import platform.posix.lseek

internal object LinuxSeekFd : FileSystemOperationHandler<SeekFd, SeekError, Long> {
    override fun invoke(input: SeekFd): Either<SeekError, Long> {
        if (input.fileDelta > Int.MAX_VALUE) {
            return Overflow("input.fileDelta too big. Request: $input").left()
        }

        val offset = lseek(input.fd.fd, input.fileDelta, input.whence.toPosixWhence())

        return if (offset >= 0) {
            offset.right()
        } else {
            errno.errnoToSeekError(input).left()
        }
    }
}
