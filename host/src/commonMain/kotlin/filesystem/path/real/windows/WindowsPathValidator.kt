/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.path.PathError

internal object WindowsPathValidator {
    internal fun validate(
        @Suppress("UnusedParameter") path: String,
    ): Either<PathError, Unit> = either {
        // TODO
        // XXX empty path is a valid path NT on Windows but "." is not
    }
}
