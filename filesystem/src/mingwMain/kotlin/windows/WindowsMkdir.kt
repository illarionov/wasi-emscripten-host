/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.pathresolver.resolveRealPath

internal class WindowsMkdir(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<Mkdir, MkdirError, Unit> {
    override fun invoke(input: Mkdir): Either<MkdirError, Unit> {
        return fsState.pathResolver.resolveRealPath(input.baseDirectory, input.path)
            .flatMap { TODO() }
    }
}
