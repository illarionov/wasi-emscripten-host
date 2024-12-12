/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsCloseFd(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<CloseFd, CloseError, Unit> {
    override fun invoke(input: CloseFd): Either<CloseError, Unit> {
        return fsState.remove(input.fd).flatMap(FdResource::close)
    }
}
