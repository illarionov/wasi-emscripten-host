/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.SystemEnvProvider
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.test.assertions.byteAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EnvironSizesGetFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()
    private val envSizesGetFunctionHandle = EnvironSizesGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun envSizesGet_success_case() {
        val testEnv = mapOf(
            "ARG1" to "VAL1",
            "A\" RG2 \"" to "VAL 2",
            "ARG=3" to "VAL==3",
            "" to "VAL 4",
            "ARG5" to "",
        )

        host.systemEnvProvider = SystemEnvProvider { testEnv }
        val countAddr: WasmPtr = 0x80
        val sizeAddr: WasmPtr = 0x100

        val code = envSizesGetFunctionHandle.execute(memory, countAddr, sizeAddr)

        assertThat(code).isEqualTo(SUCCESS)

        assertThat(memory.readI32(countAddr)).isEqualTo(5)
        assertThat(memory.readI32(sizeAddr)).isEqualTo(50)
    }

    @Test
    fun envSizesGet_test_empty_list() {
        host.systemEnvProvider = SystemEnvProvider { emptyMap() }
        val countAddr: WasmPtr = 0x80
        val sizeAddr: WasmPtr = 0x100

        val code = envSizesGetFunctionHandle.execute(memory, countAddr, sizeAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory).byteAt(countAddr).isEqualTo(0)
        assertThat(memory).byteAt(sizeAddr).isEqualTo(0)
    }

    @Test
    fun envSizesGet_test_single_empty_argument() {
        host.systemEnvProvider = SystemEnvProvider { mapOf("" to "") }
        val countAddr: WasmPtr = 0x80
        val sizeAddr: WasmPtr = 0x100

        val code = envSizesGetFunctionHandle.execute(memory, countAddr, sizeAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory.readI32(countAddr)).isEqualTo(1)
        assertThat(memory).byteAt(sizeAddr).isEqualTo(2)
    }
}
