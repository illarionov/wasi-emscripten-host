/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.chmod.Chmod

internal class WindowsChmod(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> {
        return pathResolver.getWindowsPath(input.baseDirectory, input.path)
            .flatMap { NotSupported("Not supported by file system").left() }
    }
}
