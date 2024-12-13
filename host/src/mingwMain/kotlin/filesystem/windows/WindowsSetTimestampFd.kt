/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsSetTimestampFd(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<SetTimestampFd, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestampFd): Either<SetTimestampError, Unit> =
        fsState.executeWithResource(input.fd) {
            it.setTimestamp(input.atimeNanoseconds, input.mtimeNanoseconds)
        }
}
