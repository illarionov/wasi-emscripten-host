/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxRename
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxRename(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Rename, RenameError, Unit> {
    override fun invoke(input: Rename): Either<RenameError, Unit> {
        return fsExecutor.executeWithPath(
            input.oldPath,
            input.oldBaseDirectory,
            false,
            ResolvePathError::toResolveRelativePathErrors,
        ) { oldRealPath, oldBaseDirectory, _ ->
            fsExecutor.executeWithPath(
                input.newPath,
                input.newBaseDirectory,
                false,
                ResolvePathError::toResolveRelativePathErrors,
            ) { newRealPath, newBaseDirectory, _ ->
                linuxRename(oldBaseDirectory.nativeFd, oldRealPath, newBaseDirectory.nativeFd, newRealPath)
            }
        }
    }
}
