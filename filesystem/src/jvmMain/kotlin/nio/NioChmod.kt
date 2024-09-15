/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.ext.fileModeToPosixFilePermissions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.chmod.Chmod
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.setPosixFilePermissions

internal class NioChmod(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> {
        val path: Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft(ResolvePathError::toCommonError)
            .getOrElse { return it.left() }
        return setPosixFilePermissions(path, input.mode)
    }

    companion object {
        fun setPosixFilePermissions(
            path: Path,
            @FileMode mode: Int,
        ): Either<ChmodError, Unit> = Either.catch {
            path.setPosixFilePermissions(mode.fileModeToPosixFilePermissions())
            Unit
        }.mapLeft {
            when (it) {
                is UnsupportedOperationException -> PermissionDenied("Read-only channel")
                is ClassCastException -> InvalidArgument("Invalid flags")
                is IOException -> IoError("I/O exception: ${it.message}")
                is SecurityException -> AccessDenied("Security Exception")
                else -> throw IllegalStateException("Unexpected error", it)
            }
        }
    }
}
