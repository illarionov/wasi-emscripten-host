/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.delegatefs

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.NotImplemented
import at.released.weh.filesystem.op.FileSystemOperation

internal class DelegateOperationsFileSystem(
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>>,
    interceptors: List<FileSystemInterceptor>,
) : FileSystem {
    private val interceptors: List<FileSystemInterceptor> = buildList {
        addAll(interceptors)
        add(ExecuteOperationInterceptor(operations))
    }

    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> {
        val chain = InterceptorChain(
            operation = operation,
            input = input,
            interceptors = interceptors,
        )
        return chain.proceed(input)
    }

    override fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean {
        return operations.containsKey(operation)
    }

    override fun close() = Unit

    private class ExecuteOperationInterceptor(
        private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>>,
    ) : FileSystemInterceptor {
        @Suppress("UNCHECKED_CAST")
        override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(chain: Chain<I, E, R>): Either<E, R> {
            val handler = operations[chain.operation] as? FileSystemOperationHandler<I, E, R>
            if (handler == null) {
                return NotImplemented.left() as Either<E, Nothing>
            }

            return handler.invoke(chain.input)
        }
    }
}
