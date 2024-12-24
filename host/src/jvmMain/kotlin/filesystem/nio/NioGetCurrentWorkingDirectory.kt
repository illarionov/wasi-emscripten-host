/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.toGetCwdError
import at.released.weh.filesystem.path.toResolvePathError
import at.released.weh.filesystem.path.virtual.VirtualPath

internal class NioGetCurrentWorkingDirectory(
    private val jvmPathResolver: JvmPathResolver,
    private val pathConverter: NioPathConverter,
) : FileSystemOperationHandler<GetCurrentWorkingDirectory, GetCurrentWorkingDirectoryError, VirtualPath> {
    private val currentDirectoryVirtualPath = VirtualPath.create(".").getOrElse {
        error("Can not create current directory path")
    }

    override fun invoke(input: GetCurrentWorkingDirectory): Either<GetCurrentWorkingDirectoryError, VirtualPath> {
        return jvmPathResolver.resolve(currentDirectoryVirtualPath, BaseDirectory.CurrentWorkingDirectory, true)
            .mapLeft { it.toGetCwdError() }
            .flatMap { nioRealPath ->
                pathConverter.toVirtualPath(nioRealPath).mapLeft { it.toResolvePathError().toGetCwdError() }
            }
    }

    private fun OpenError.toGetCwdError(): GetCurrentWorkingDirectoryError = when (this) {
        is GetCurrentWorkingDirectoryError -> this
        else -> InvalidArgument("Error `${this.message}`")
    }
}
