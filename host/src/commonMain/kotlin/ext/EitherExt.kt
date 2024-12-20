/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("IDENTIFIER_LENGTH")

package at.released.weh.ext

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.handleErrorWith

/**
 * [Either.handleErrorWith] with inline modifier (arrow [#3547](https://github.com/arrow-kt/arrow/issues/3547))
 */
internal inline fun <A, B, C> Either<A, B>.flatMapLeft(f: (A) -> Either<C, B>): Either<C, B> {
    return when (this) {
        is Left -> f(this.value)
        is Right -> this
    }
}
