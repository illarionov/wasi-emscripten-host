/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.ext.fileModeAsFileAttributesIfSupported
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.path.withResolvePathErrorAsCommonError
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

internal class NioMkdir(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Mkdir, MkdirError, Unit> {
    override fun invoke(input: Mkdir): Either<MkdirError, Unit> =
        fsState.executeWithPath(input.baseDirectory, input.path) { resolvePathResult ->
            resolvePathResult.withResolvePathErrorAsCommonError()
                .flatMap { mkdir(it.nio, input.mode, input.failIfExists) }
        }

    private fun mkdir(
        path: Path,
        @FileMode mode: Int,
        failIfExists: Boolean,
    ): Either<MkdirError, Unit> {
        val modeAttrs = path.fileSystem.fileModeAsFileAttributesIfSupported(mode)
        val result = Either.catch {
            @Suppress("SpreadOperator")
            Files.createDirectory(path, *modeAttrs)
            Unit
        }.mapLeft {
            when (it) {
                is UnsupportedOperationException -> PermissionDenied("Unsupported file mode")
                is FileAlreadyExistsException -> Exists("`$path` exists")
                is IOException -> IoError("I/O error: ${it.message}")
                else -> throw IllegalStateException("Unexpected error", it)
            }
        }

        return if (!failIfExists && result.leftOrNull() is Exists) {
            Unit.right()
        } else {
            result
        }
    }
}
