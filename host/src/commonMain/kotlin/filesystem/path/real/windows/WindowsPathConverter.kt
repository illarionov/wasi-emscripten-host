/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath.Companion.pathNoPrefix
import at.released.weh.filesystem.path.virtual.VirtualPath

internal object WindowsPathConverter {
    internal fun toVirtualPath(
        realPath: WindowsRealPath,
        dropWindowsPrefix: Boolean = true,
    ): Either<PathError, VirtualPath> {
        val realPathString = if (dropWindowsPrefix) {
            realPath.pathNoPrefix
        } else {
            realPath.kString
        }

        val unixPath = normalizeVirtualPathSlashes(realPathString)
        return VirtualPath.create(unixPath)
    }

    /**
     * Guess Windows real path from [VirtualPath].
     *
     * Examples:
     * * Virtual path: `/Windows/System32`, Windows Real Path: `\Windows\System32`
     * * Virtual path: `D:/Users/Public`, Windows real path: `D:\Users\Public`
     */
    internal fun fromVirtualPath(
        path: VirtualPath,
    ): Either<PathError, WindowsRealPath> {
        return WindowsRealPath.create(normalizeWindowsSlashes(path.toString()))
            .flatMap(WindowsRealPath::normalize)
    }

    internal fun normalizeVirtualPathSlashes(
        path: String,
    ): String = path.replace('\\', '/')

    internal fun normalizeWindowsSlashes(
        path: String,
    ): String = path.replace('/', '\\')
}
