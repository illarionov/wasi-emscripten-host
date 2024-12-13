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
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute

internal fun validateSymlinkTarget(
    target: VirtualPath,
    allowAbsolutePath: Boolean,
): Either<SymlinkError, Unit> = either {
    if (!allowAbsolutePath && target.isAbsolute()) {
        raise(InvalidArgument("link destination should be relative"))
    }
}
