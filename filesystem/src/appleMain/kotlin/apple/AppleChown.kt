/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleChown
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.chown.Chown

internal class AppleChown(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Chown, ChownError, Unit> {
    override fun invoke(input: Chown): Either<ChownError, Unit> =
        fsState.executeWithPath(input.path, input.baseDirectory) { realPath, baseDirectory ->
            appleChown(baseDirectory, realPath, input.owner, input.group, input.followSymlinks)
        }
}
