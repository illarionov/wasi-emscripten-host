/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath

internal interface FileSystemActionExecutor {
    fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        followBaseSymlink: Boolean = true,
        errorMapper: (ResolvePathError) -> E,
        block: ExecutionBlock<E, R>,
    ): Either<E, R>

    fun interface ExecutionBlock<E : FileSystemOperationError, R : Any> {
        operator fun invoke(
            path: PosixRealPath,
            baseDirectory: PosixDirectoryChannel,
            nativeFollowBaselink: Boolean,
        ): Either<E, R>
    }
}
