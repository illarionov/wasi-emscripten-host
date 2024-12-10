/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.PathError.InvalidPathFormat
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException

internal class NioPathConverter(
    private val javaFs: FileSystem = FileSystems.getDefault(),
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(javaFs),
) {
    fun toRealPath(virtualPath: VirtualPath): Either<PathError, NioRealPath> {
        return try {
            pathFactory.create(javaFs.getPath(virtualPath.toString())).right()
        } catch (@Suppress("SwallowedException") ipe: InvalidPathException) {
            return InvalidPathFormat("Path `$virtualPath` cannot be converted").left()
        }
    }

    internal fun toVirtualPath(
        path: NioRealPath,
    ): Either<PathError, VirtualPath> {
        return VirtualPath.create(path.kString).mapLeft { InvalidPathFormat(it.message) }
    }
}
