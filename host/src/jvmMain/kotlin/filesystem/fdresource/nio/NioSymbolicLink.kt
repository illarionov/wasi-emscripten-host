/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.SymlinkError
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

internal fun createSymlink(
    linkpath: Path,
    target: Path,
    allowAbsoluteOldPath: Boolean,
): Either<SymlinkError, Unit> {
    return validateSymlinkTarget(target, allowAbsoluteOldPath)
        .flatMap {
            createSymlink(linkpath, target)
        }
}

private fun validateSymlinkTarget(
    target: Path,
    allowAbsolutePath: Boolean,
): Either<SymlinkError, Unit> = either {
    // XXX Path.of("/").isAbsolute is false on Windows
    if (!allowAbsolutePath && (target.isAbsolute || target.startsWith("/"))) {
        raise(InvalidArgument("link destination should be relative"))
    }
}

private fun createSymlink(
    linkpath: Path,
    target: Path,
): Either<SymlinkError, Unit> {
    return Either.catch {
        Files.createSymbolicLink(linkpath, target)
        Unit
    }.mapLeft {
        when (it) {
            // AccessDeniedException thrown on Windows when linkpath exists
            is AccessDeniedException -> NoEntry("Access denied")
            is UnsupportedOperationException -> PermissionDenied("Filesystem does not support symbolic links")
            is FileAlreadyExistsException -> Exists("Link path already exists")
            is IOException -> IoError("I/o exception `${it.message}`")
            else -> IoError("Other error `${it.message}`")
        }
    }
}
