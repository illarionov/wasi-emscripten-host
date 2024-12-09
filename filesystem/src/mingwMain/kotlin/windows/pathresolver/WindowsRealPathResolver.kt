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
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError

internal fun WindowsPathResolver.resolveRealPath(
    directory: BaseDirectory,
    path: VirtualPath,
): Either<ResolveRelativePathErrors, WindowsRealPath> {
    val windowsPath = WindowsPathConverter.convertToRealPath(path)
    if (path.isAbsolute()) {
        return windowsPath.right()
    }
    return resolvePath(directory, windowsPath)
}

internal fun WindowsPathResolver.resolvePath(
    directory: BaseDirectory,
    relativePath: WindowsRealPath,
): Either<ResolveRelativePathErrors, WindowsRealPath> {
    return either {
        val baseChannel = resolveBaseDirectory(directory).bind()
            ?: raise(BadFileDescriptor("Can not resolve relative path: current directory is not open"))
        val baseChannelPath = baseChannel.handle.getFinalPath()
            .mapLeft(GetFinalPathError::toResolveRelativePathError)
            .bind()
        return WindowsRealPath.create(baseChannelPath, relativePath.kString)
            .mapLeft<ResolveRelativePathErrors>(PathError::toCommonError)
    }
}
