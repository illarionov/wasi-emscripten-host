/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.RealPath
import kotlinx.io.bytestring.ByteString

internal class WindowsNtRealPath private constructor(
    utf16Chars: CharArray,
) : WindowsRealPath(utf16Chars) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as WindowsNtRealPath

        return utf16Chars.contentEquals(other.utf16Chars)
    }

    override fun hashCode(): Int {
        return utf16Chars.contentHashCode()
    }

    internal companion object : RealPath.Factory<WindowsNtRealPath> {
        override fun create(bytes: ByteString): Either<PathError, WindowsNtRealPath> {
            return Either.catch { bytes.toByteArray().decodeToString(throwOnInvalidSequence = true) }
                .mapLeft { PathError.InvalidPathFormat("Path is not a valid Unicode string") }
                .flatMap<PathError, String, WindowsNtRealPath>(::create)
        }

        // TODO
        override fun create(path: String): Either<PathError, WindowsNtRealPath> {
            return WindowsNtRealPath(path.toCharArray()).right()
        }
    }
}
