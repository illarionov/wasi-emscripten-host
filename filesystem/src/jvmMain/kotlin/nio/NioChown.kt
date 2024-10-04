/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.fdresource.nio.nioSetPosixUserGroup
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.chown.Chown
import java.nio.file.Path

internal class NioChown(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Chown, ChownError, Unit> {
    override fun invoke(input: Chown): Either<ChownError, Unit> {
        val path: Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft(ResolvePathError::toCommonError)
            .getOrElse { return it.left() }
        return nioSetPosixUserGroup(fsState.javaFs, path, input.owner, input.group)
    }
}
