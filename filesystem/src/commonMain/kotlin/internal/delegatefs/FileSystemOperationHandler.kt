/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.delegatefs

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError

internal fun interface FileSystemOperationHandler<
        in I : Any,
        out E : FileSystemOperationError,
        out R : Any,
        > {
    public operator fun invoke(input: I): Either<E, R>
}
