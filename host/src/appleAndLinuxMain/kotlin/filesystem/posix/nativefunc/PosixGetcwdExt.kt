/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath

internal fun getCurrentWorkingDirectoryVirtualPath(): Either<OpenError, VirtualPath> {
    return posixGetcwd()
        .mapLeft { it.toOpenError() }
        .flatMap { absolutePath ->
            toVirtualPath(absolutePath)
                .mapLeft(PathError::toResolveRelativePathErrors)
        }
}

private fun GetCurrentWorkingDirectoryError.toOpenError(): OpenError = if (this is OpenError) {
    this
} else {
    InvalidArgument("Can not get current working directory")
}
