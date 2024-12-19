/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.posix

import arrow.core.Either
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.virtual.VirtualPath

internal object PosixPathConverter {
    fun toRealPath(virtualPath: VirtualPath): Either<PathError, PosixRealPath> {
        return PosixRealPath.create(virtualPath.utf8Bytes)
    }

    fun toVirtualPath(path: PosixRealPath): Either<PathError, VirtualPath> {
        return VirtualPath.Companion.create(path.utf8Bytes)
    }
}
