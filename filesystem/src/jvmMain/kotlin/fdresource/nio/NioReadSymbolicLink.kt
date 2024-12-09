/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadLinkError
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NotLinkException
import java.nio.file.Path

internal fun readSymbolicLink(
    path: Path,
): Either<ReadLinkError, Path> {
    return Either.catch {
        Files.readSymbolicLink(path)
    }.mapLeft {
        when (it) {
            is UnsupportedOperationException -> InvalidArgument("Symbolic links are not supported")
            is NotLinkException -> InvalidArgument("File `$path` is not a symlink")
            is IOException -> IoError("I/o error while read symbolic link of `$path`")
            is SecurityException -> AccessDenied("Permission denied `$path`")
            else -> throw IllegalStateException("Unexpected error", it)
        }
    }
}
