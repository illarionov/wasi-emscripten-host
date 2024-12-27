/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toResolvePathError
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Implementation of FileSystemActionExecutor that uses system path resolving for environments
 * where RESOLVE_BENEATH is available or when filesystem path sandboxing is not required.
 */
internal class DirectFileSystemActionExecutor(
    private val pathResolver: PosixPathResolver
) : FileSystemActionExecutor {
    override fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        errorMapper: (ResolvePathError) -> E,
        block: (path: PosixRealPath, baseDirectory: PosixDirectoryChannel) -> Either<E, R>,
    ): Either<E, R> {
        return PosixPathConverter.toRealPath(path)
            .mapLeft { errorMapper(it.toResolvePathError()) }
            .flatMap { realPath ->
                pathResolver.getBaseDirectory(baseDirectory)
                    .mapLeft { errorMapper(it) }
                    .flatMap { nativeFd ->
                        block(realPath, nativeFd)
                    }
            }
    }
}
