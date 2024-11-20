/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.chown.Chown
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsChown(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<Chown, ChownError, Unit> {
    override fun invoke(input: Chown): Either<ChownError, Unit> =
        fsState.executeWithBaseDirectoryResource(input.baseDirectory) {
            // TODO: implement on windows?
            NotSupported("Not supported by file system").left()
        }
}
