/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.delegatefs

import arrow.core.Either
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.FileSystemOperation

internal class InterceptorChain<I : Any, out E : FileSystemOperationError, out R : Any>(
    override val operation: FileSystemOperation<I, E, R>,
    override val input: I,
    private val interceptors: List<FileSystemInterceptor>,
    private val index: Int = 0,
) : FileSystemInterceptor.Chain<I, E, R> {
    override fun proceed(input: I): Either<E, R> {
        val interceptor = interceptors.getOrNull(index) ?: error("End of interceptor chain")
        val next = InterceptorChain(
            interceptors = interceptors,
            index = index + 1,
            operation = operation,
            input = input,
        )
        return interceptor.intercept(next)
    }
}
