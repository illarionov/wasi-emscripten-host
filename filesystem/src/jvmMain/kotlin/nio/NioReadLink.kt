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
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.readlink.ReadLink
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NotLinkException

internal class NioReadLink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadLink, ReadLinkError, String> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, String> {
        val path: java.nio.file.Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft(ResolvePathError::toCommonError)
            .getOrElse { return it.left() }

        return Either.catch {
            Files.readSymbolicLink(path).toString()
        }.mapLeft {
            when (it) {
                is UnsupportedOperationException -> InvalidArgument("Symbolic links are not supported")
                is NotLinkException -> InvalidArgument("File `$path` is not a symlink")
                is IOException -> IoError("I/o error while read symbolink link of `$path`")
                is SecurityException -> AccessDenied("Permission denied `$path`")
                else -> throw IllegalStateException("Unexpected error", it)
            }
        }
    }
}
