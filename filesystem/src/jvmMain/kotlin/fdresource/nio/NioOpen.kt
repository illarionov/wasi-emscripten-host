/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.TooManySymbolicLinks
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.FileAlreadyExistsException
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

internal fun nioOpenFile(
    path: Path,
    options: Set<OpenOption>,
    fileAttributes: Array<FileAttribute<*>>,
): Either<OpenError, FileChannel> = Either.catch {
    @Suppress("SpreadOperator")
    FileChannel.open(path, options, *fileAttributes)
}.mapLeft { error -> error.openCreateErrorToOpenError(path) }

internal fun Throwable.openCreateErrorToOpenError(path: Path): OpenError = when (this) {
    is IllegalArgumentException -> InvalidArgument("Can not open `$path`: invalid combination of options ($message)")
    is UnsupportedOperationException -> InvalidArgument("Can not open `$path`: unsupported operation ($message)")
    is FileAlreadyExistsException -> Exists("`$path` already exists ($message)")
    is IOException -> if (message?.contains("NOFOLLOW_LINKS specified") == true) {
        TooManySymbolicLinks("Can not open `$path`: too many symbolic links ($message)")
    } else {
        IoError("Can not open `$path`: I/O error ($message)")
    }
    is SecurityException -> PermissionDenied("Can not open `$path`: Permission denied ($message)")
    else -> throw IllegalStateException("Unexpected error", this)
}
