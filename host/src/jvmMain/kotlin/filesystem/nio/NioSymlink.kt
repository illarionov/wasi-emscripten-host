/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.fdresource.nio.createSymlink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.withPathErrorAsCommonError

internal class NioSymlink(
    private val fsState: NioFileSystemState,
    private val pathConverter: NioPathConverter = NioPathConverter(fsState.javaFs),
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return fsState.executeWithPath(
            input.newPathBaseDirectory,
            input.newPath,
        ) { resolvedPath: Either<OpenError, NioRealPath> ->
            val result = resolvedPath.flatMap { newRealPath: NioRealPath ->
                    pathConverter.toRealPath(input.oldPath)
                        .withPathErrorAsCommonError()
                        .map { oldRealPath -> newRealPath to oldRealPath }
                }
                .mapLeft(OpenError::toSymlinkError)
                .flatMap { (newPath: NioRealPath, oldPath: NioRealPath) ->
                    createSymlink(newPath.nio, oldPath.nio, input.allowAbsoluteOldPath)
                }
            result
        }
    }
}

private fun OpenError.toSymlinkError(): SymlinkError = when (this) {
    is SymlinkError -> this
    else -> IoError(this.message)
}
