/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.fdresource.nio.nioSetTimestamp
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import java.nio.file.Path

internal class NioSetTimestamp(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SetTimestamp, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestamp): Either<SetTimestampError, Unit> {
        val path: Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft(ResolvePathError::toCommonError)
            .getOrElse { return it.left() }
        return nioSetTimestamp(path, input.followSymlinks, input.atimeNanoseconds, input.mtimeNanoseconds)
    }
}
