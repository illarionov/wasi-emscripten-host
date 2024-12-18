/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows.nt

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.path.PathError
import kotlin.jvm.JvmInline

/**
 * Windows NT Object Manager path.
 *
 * Absolute path starts with '\'.
 *
 * Examples: `\??\C:\Windows`, `\Device\HarddiskVolume2\Windows`, etc.
 */
@JvmInline
internal value class WindowsNtObjectManagerPath private constructor(
    val kString: String,
) {
    override fun toString(): String {
        return kString
    }

    internal companion object {
        fun create(path: String): Either<PathError, WindowsNtObjectManagerPath> {
            return validatePath(path).map { WindowsNtObjectManagerPath(path) }
        }

        fun validatePath(path: String): Either<PathError, Unit> = either {
            if (path.isEmpty()) {
                raise(PathError.EmptyPath())
            }
            if (!path.startsWith("""\""")) {
                raise(PathError.InvalidPathFormat("Absolute NT Object Manager path should start with `\\`"))
            }
        }
    }
}
