/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory

internal class JvmCurrentDirectoryProvider(
    private val javaFs: java.nio.file.FileSystem,
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(javaFs),
) : CurrentDirectoryProvider {
    override fun getCurrentWorkingDirectory(): Either<GetCurrentWorkingDirectoryError, NioRealPath> {
        return pathFactory.create(javaFs.getPath(".").toAbsolutePath()).right()
    }
}
