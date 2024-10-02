/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.EntropySource
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Errno.INVAL
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.test.assertions.hasBytesAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RandomGetFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()
    private val randomGetHandle = RandomGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun randomGet_success_case() {
        val testBufSize = 32
        val testEntropy = ByteArray(testBufSize) { (it + 3).toByte() }
        host.entropySource = EntropySource { size ->
            check(size == testBufSize)
            testEntropy
        }

        val bufPtr: WasmPtr = 128

        val code = randomGetHandle.execute(memory, bufPtr, testBufSize)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory).hasBytesAt(bufPtr, testEntropy)
    }

    @Test
    fun randomGet_should_return_correct_code_on_fail() {
        host.entropySource = EntropySource { error("No entropy source") }

        val bufPtr: WasmPtr = 128

        val code = randomGetHandle.execute(memory, bufPtr, -32)
        assertThat(code).isEqualTo(INVAL)
    }
}
