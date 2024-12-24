/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdresource.nio.nioSetPosixFilePermissions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.chmod.Chmod

internal class NioChmod(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> =
        fsState.executeWithPath(input.baseDirectory, input.path) { resolvePathResult ->
            resolvePathResult.mapLeft(OpenError::toChmodError)
                .flatMap { path -> nioSetPosixFilePermissions(path.nio, input.mode) }
        }
}

private fun OpenError.toChmodError(): ChmodError = when (this) {
    is ChmodError -> this
    else -> IoError(this.message)
}
