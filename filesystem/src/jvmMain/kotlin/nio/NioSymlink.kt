/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.fdresource.nio.createSymlink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.symlink.Symlink
import java.nio.file.Path

internal class NioSymlink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return fsState.executeWithPath(
            input.newPathBaseDirectory,
            input.newPath,
        ) { resolvedPath: Either<ResolvePathError, Path> ->
            resolvedPath
                .mapLeft(ResolvePathError::toCommonError)
                .flatMap {
                    createSymlink(it, input.oldPath, input.allowAbsoluteOldPath)
                }
        }
    }
}
