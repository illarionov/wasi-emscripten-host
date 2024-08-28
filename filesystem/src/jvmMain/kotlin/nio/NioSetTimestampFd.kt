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
import at.released.weh.filesystem.nio.NioSetTimestamp.Companion.setTimestamp
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import java.nio.file.Path

internal class NioSetTimestampFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SetTimestampFd, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestampFd): Either<SetTimestampError, Unit> {
        val path: Path = fsState.fileDescriptors.get(input.fd)?.path
            ?: return BadFileDescriptor("File descriptor `${input.fd}` is not opened").left()
        return setTimestamp(path, input.followSymlinks, input.atimeNanoseconds, input.mtimeNanoseconds)
    }
}
