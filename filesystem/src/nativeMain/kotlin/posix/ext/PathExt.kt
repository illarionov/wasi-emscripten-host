/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.ext

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.preopened.VirtualPath

internal fun validatePath(path: VirtualPath): Either<InvalidArgument, Unit> = either {
    if (!path.all(Char::isValidPathChar)) {
        raise(InvalidArgument("Path contains invalid characters"))
    }

    return if (path.isEmpty()) {
        InvalidArgument("Path is empty").left()
    } else {
        Unit.right()
    }
}

private fun Char.isValidPathChar() = this != 0.toChar()
