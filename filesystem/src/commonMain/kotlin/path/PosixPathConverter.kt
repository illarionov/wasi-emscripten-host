/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import kotlinx.io.bytestring.decodeToString

internal object PosixPathConverter {
    // XXX should be removed?
    fun convertToRealPath(virtualPath: String): Either<InvalidArgument, RealPath> {
        return VirtualPath.of(virtualPath)
            .mapLeft { InvalidArgument(it.message) }
            .flatMap { toRealPath(it) }
    }

    fun toRealPath(virtualPath: VirtualPath): Either<InvalidArgument, RealPath> {
        return virtualPath.utf8.decodeToString().right()
    }

    fun convertToVirtualPath(path: RealPath): Either<InvalidArgument, VirtualPath> {
        return VirtualPath.of(path).mapLeft { InvalidArgument(it.message) }
    }
}
