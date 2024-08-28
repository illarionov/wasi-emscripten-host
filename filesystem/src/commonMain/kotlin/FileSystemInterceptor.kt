/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_MULTIPLE_MODIFIERS_ORDER")

package at.released.weh.filesystem

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.FileSystemOperation

public interface FileSystemInterceptor {
    public fun <I : Any, E : FileSystemOperationError, R : Any> intercept(
        chain: Chain<I, E, R>,
    ): Either<E, R>

    public interface Chain<I : Any, out E : FileSystemOperationError, out R : Any> {
        public val operation: FileSystemOperation<I, E, R>
        public val input: I

        public fun proceed(input: I): Either<E, R>
    }
}
