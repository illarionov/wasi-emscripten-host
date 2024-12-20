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
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class AppleRename(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Rename, RenameError, Unit> {
    override fun invoke(input: Rename): Either<RenameError, Unit> {
        return fsExecutor.executeWithPath(
            input.oldPath,
            input.oldBaseDirectory,
            ResolvePathError::toResolveRelativePathErrors,
        ) { oldRealPath, oldBaseDirectory ->
            fsExecutor.executeWithPath(
                input.newPath,
                input.newBaseDirectory,
                ResolvePathError::toResolveRelativePathErrors,
            ) { newRealPath, newBaseDirectory ->
                appleRename(oldBaseDirectory.nativeFd, oldRealPath, newBaseDirectory.nativeFd, newRealPath)
            }
        }
    }
}
