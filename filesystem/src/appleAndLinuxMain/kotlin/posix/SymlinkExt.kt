/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.SymlinkError

internal fun validateSymlinkTarget(
    target: String,
    allowAbsolutePath: Boolean,
): Either<SymlinkError, Unit> = either {
    val cleanedTarget = target.trim()
    if (!allowAbsolutePath && cleanedTarget.startsWith("/")) {
        raise(InvalidArgument("link destination should be relative"))
    }
}
