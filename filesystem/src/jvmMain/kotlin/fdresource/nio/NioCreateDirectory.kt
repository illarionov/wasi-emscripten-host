/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.OpenError
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

internal fun nioCreateDirectory(
    path: Path,
    fileAttributes: Array<FileAttribute<*>>,
): Either<OpenError, Path> = Either.catch {
    @Suppress("SpreadOperator")
    Files.createDirectory(path, *fileAttributes)
}.mapLeft { error -> error.openCreateErrorToOpenError(path) }
