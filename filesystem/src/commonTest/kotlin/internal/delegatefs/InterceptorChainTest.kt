/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.delegatefs

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.FileSystemOperation
import kotlin.test.Test

class InterceptorChainTest {
    @Test
    @Suppress("UNCHECKED_CAST")
    fun interceptors_should_be_executed_in_correct_order() {
        val operations: MutableList<String> = mutableListOf()

        class TestInterceptor(val interceptorNo: Int) : FileSystemInterceptor {
            override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(
                chain: Chain<I, E, R>,
            ): Either<E, R> {
                val inputTestOp = chain.input as TestFsOp
                operations.add("Interceptor$interceptorNo: Before. Original input: ${inputTestOp.path}")
                val modifiedInput = TestFsOp("inp$interceptorNo-${inputTestOp.path}")

                val result = chain.proceed(modifiedInput as I)

                operations.add("Interceptor$interceptorNo: After. Unmodified result: ${result.getOrNull()}")
                return result.map { "$it-outp$interceptorNo" as R }
            }
        }

        val lastInterceptor = object : FileSystemInterceptor {
            override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(
                chain: Chain<I, E, R>,
            ): Either<E, R> {
                val inputTestOp = chain.input as TestFsOp
                operations.add(("Operation. Input: ${inputTestOp.path}"))
                return "link".right() as Either<E, R>
            }
        }

        val interceptors = listOf(
            TestInterceptor(1),
            TestInterceptor(2),
            TestInterceptor(3),
            lastInterceptor,
        )

        val input = TestFsOp("Test")
        val chain = InterceptorChain(TestFsOp, input, interceptors)
        val result = chain.proceed(input)

        assertThat(operations).containsExactly(
            "Interceptor1: Before. Original input: Test",
            "Interceptor2: Before. Original input: inp1-Test",
            "Interceptor3: Before. Original input: inp2-inp1-Test",
            "Operation. Input: inp3-inp2-inp1-Test",
            "Interceptor3: After. Unmodified result: link",
            "Interceptor2: After. Unmodified result: link-outp3",
            "Interceptor1: After. Unmodified result: link-outp3-outp2",
        )
        assertThat(result.getOrNull()).isEqualTo("link-outp3-outp2-outp1")
    }

    data class TestFsOp(
        val path: String,
    ) {
        companion object : FileSystemOperation<TestFsOp, FileSystemOperationError, String> {
            override val tag: String get() = "testop"
        }
    }
}
