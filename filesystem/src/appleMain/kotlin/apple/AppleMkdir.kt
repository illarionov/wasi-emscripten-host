/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleMkdir
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.mkdir.Mkdir

internal class AppleMkdir(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Mkdir, MkdirError, Unit> {
    override fun invoke(input: Mkdir): Either<MkdirError, Unit> =
        fsState.executeWithBaseDirectoryResource(input.baseDirectory) {
            appleMkdir(it, input.path, input.mode, input.failIfExists)
        }
}
