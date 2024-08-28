/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.ext

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.Errno

internal fun Either<FileSystemOperationError, Unit>.negativeErrnoCode(): Int = this.fold(
    ifLeft = { -it.errno.code },
    ifRight = { Errno.SUCCESS.code },
)
