/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath.Companion.pathNoPrefix
import at.released.weh.filesystem.path.virtual.VirtualPath

internal object WindowsPathConverter {
    internal fun convertToVirtualPath(
        realPath: WindowsRealPath,
    ): Either<PathError, VirtualPath> {
        val unixPath = realPath.pathNoPrefix.replace('\\', '/')
        return VirtualPath.create(unixPath)
    }
}
