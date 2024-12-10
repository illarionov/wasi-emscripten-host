/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.posix

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import at.released.weh.filesystem.path.PathError
import kotlinx.io.bytestring.ByteString

internal object PosixPathValidator {
    internal fun validate(
        utf8ByteString: ByteString,
    ): Either<PathError, Unit> = Either.catch {
        utf8ByteString.toByteArray().decodeToString(throwOnInvalidSequence = true)
    }
        .mapLeft { PathError.InvalidPathFormat("Path is not a valid Unicode string") }
        .flatMap { pathString -> validate(pathString) }

    fun validate(path: String): Either<PathError, Unit> = either {
        if (path.isEmpty()) {
            raise(PathError.EmptyPath("Path is empty"))
        }

        if (path.contains(0.toChar())) {
            raise(PathError.InvalidPathFormat("Null character is not allowed in path"))
        }
    }
}
