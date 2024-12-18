/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows.nt

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.windows.WindowsPathType
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.normalizeWindowsPath
import at.released.weh.filesystem.path.toResolvePathError
import kotlin.jvm.JvmInline

@JvmInline
internal value class WindowsNtRelativePath private constructor(
    val kString: String,
) {
    override fun toString(): String {
        return kString
    }

    internal companion object {
        val CURRENT = WindowsNtRelativePath("")

        fun createFromRelativeWindowsPath(path: WindowsRealPath): Either<ResolvePathError, WindowsNtRelativePath> =
            when (path.type) {
                WindowsPathType.RELATIVE -> normalizeWindowsPath(path.kString)
                    .flatMap(::create)
                    .mapLeft(PathError::toResolvePathError)

                else -> PathError.InvalidPathFormat("Path is not relative").left()
            }

        fun create(path: String): Either<PathError, WindowsNtRelativePath> {
            return validatePath(path).map { WindowsNtRelativePath(path) }
        }

        fun validatePath(path: String): Either<PathError, Unit> = either {
            if (path.startsWith("""\""")) {
                raise(PathError.AbsolutePath("Path should not be absolute"))
            }
        }
    }
}
