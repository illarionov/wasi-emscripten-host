/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.path

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.path.virtual.ValidateVirtualPathError
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.nio.file.InvalidPathException
import java.nio.file.Path

internal class JvmNioPathConverter(
    private val javaFs: java.nio.file.FileSystem,
) {
    fun toNioPath(virtualPath: VirtualPath): Either<InvalidArgument, Path> {
        return try {
            javaFs.getPath(virtualPath.toString()).right()
        } catch (@Suppress("SwallowedException") ipe: InvalidPathException) {
            return InvalidArgument("Path `$virtualPath` cannot be converted").left()
        }
    }

    fun toVirtualPath(path: Path): Either<ValidateVirtualPathError, VirtualPath> {
        require(!path.isAbsolute)
        return VirtualPath.of(path.toString())
    }
}
