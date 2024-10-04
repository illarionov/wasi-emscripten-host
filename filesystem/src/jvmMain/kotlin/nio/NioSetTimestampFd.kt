/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd

internal class NioSetTimestampFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SetTimestampFd, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestampFd): Either<SetTimestampError, Unit> {
        val resource = fsState.get(input.fd)
            ?: return BadFileDescriptor("File descriptor `${input.fd}` is not open").left()
        return resource.setTimestamp(input.atimeNanoseconds, input.mtimeNanoseconds)
    }
}
