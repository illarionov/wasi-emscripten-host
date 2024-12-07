/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.virtual

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.path.virtual.ValidateVirtualPathError.InvalidCharacters
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString

internal object VirtualPathValidator {
    internal fun validate(
        utf8ByteString: ByteString,
    ): Either<ValidateVirtualPathError, Unit> = either {
        val string = utf8ByteString.decodeToString()
        if (string.isEmpty()) {
            raise(ValidateVirtualPathError.PathIsEmpty())
        }

        string.forEachIndexed { index, pathChar: Char ->
            if (!pathChar.isValidPathCharacter()) {
                raise(
                    InvalidCharacters(
                        "Character at 0x${pathChar.code.toString(16)} at $index is not a valid unicode scalar value",
                    ),
                )
            }
        }

        return Unit.right()
    }

    private fun Char.isValidPathCharacter(): Boolean = this.code != 0
}
