/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.pathresolver

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.preopened.VirtualPath
import at.released.weh.filesystem.windows.win32api.ext.combinePath
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import platform.windows.PathIsRelativeW

internal fun WindowsPathResolver.resolveRealPath(
    directory: BaseDirectory,
    path: VirtualPath,
): Either<ResolveRelativePathErrors, RealPath> {
    if (PathIsRelativeW(path) == 0) {
        return path.right()
    }
    return either {
        val baseChannel = resolveBaseDirectory(directory).bind()
            ?: raise(BadFileDescriptor("Can not resolve relative path: current directory is not open"))
        val baseChannelPath = baseChannel.handle.getFinalPath()
            .mapLeft(GetFinalPathError::toResolveRelativePathError)
            .bind()
        combinePath(baseChannelPath, path)
    }
}

private fun GetFinalPathError.toResolveRelativePathError(): ResolveRelativePathErrors = when (this) {
    is GetFinalPathError.AccessDenied -> at.released.weh.filesystem.error.NotCapable(this.message)
    is GetFinalPathError.InvalidHandle -> InvalidArgument(this.message)
    is GetFinalPathError.MaxAttemptsReached -> NameTooLong(this.message)
    is GetFinalPathError.OtherError -> InvalidArgument(this.message)
}
