/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleRename
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.rename.Rename

internal class AppleRename(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Rename, RenameError, Unit> {
    override fun invoke(input: Rename): Either<RenameError, Unit> {
        return fsState.executeWithBaseDirectoryResource(input.oldBaseDirectory) { oldDirFd ->
            fsState.executeWithBaseDirectoryResource(input.newBaseDirectory) { newDirFd ->
                appleRename(oldDirFd, input.oldPath, newDirFd, input.newPath)
            }
        }
    }
}
