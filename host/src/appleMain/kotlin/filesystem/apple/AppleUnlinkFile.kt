/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleUnlinkFile
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.unlink.UnlinkFile

internal class AppleUnlinkFile(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> =
        fsState.executeWithPath(input.path, input.baseDirectory) { realPath, baseDirectory ->
            appleUnlinkFile(baseDirectory, realPath)
        }
}
