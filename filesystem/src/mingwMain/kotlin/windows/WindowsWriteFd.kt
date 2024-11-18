/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsWriteFd(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<WriteFd, WriteError, ULong> {
    override fun invoke(input: WriteFd): Either<WriteError, ULong> = fsState.executeWithResource(input.fd) {
        it.write(input.cIovecs, input.strategy)
    }
}
