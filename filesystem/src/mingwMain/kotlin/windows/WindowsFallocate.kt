/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.fallocate.FallocateFd
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsFallocate(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<FallocateFd, FallocateError, Unit> {
    override fun invoke(input: FallocateFd): Either<FallocateError, Unit> = fsState.executeWithResource(input.fd) {
        it.fallocate(input.offset, input.length)
    }
}
