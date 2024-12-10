/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.fdresource.nio.nioSetTimestamp
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toCommonError

internal class NioSetTimestamp(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SetTimestamp, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestamp): Either<SetTimestampError, Unit> =
        fsState.executeWithPath(input.baseDirectory, input.path) { resolvePathResult ->
            resolvePathResult
                .mapLeft(ResolvePathError::toCommonError)
                .flatMap {
                    nioSetTimestamp(it.nio, input.followSymlinks, input.atimeNanoseconds, input.mtimeNanoseconds)
                }
        }
}
