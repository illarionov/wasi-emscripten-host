/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.CommandArgsProvider
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.test.assertions.byteAt
import at.released.weh.wasm.core.test.assertions.hasBytesAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ArgsSizesGetHostFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()
    private val argsSizesGetHandle = ArgsSizesGetHostFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun argsGet_success_case() {
        val testArgs = listOf(
            "testproc",
            "arg1",
            "arg2 prefix \" arg2text \" arg2 postfix",
            "//arg3=\\",
        )

        host.commandArgsProvider = CommandArgsProvider { testArgs }
        val argvCountAddr: WasmPtr = 0x80
        val argvBufSizeAddr: WasmPtr = 0x100
        val code = argsSizesGetHandle.execute(memory, argvCountAddr, argvBufSizeAddr)

        assertThat(code).isEqualTo(SUCCESS)

        assertThat(memory.readI32(argvCountAddr)).isEqualTo(4)
        assertThat(memory.readI32(argvBufSizeAddr)).isEqualTo(61)
    }

    @Test
    fun argsGet_test_empty_list() {
        host.commandArgsProvider = CommandArgsProvider { emptyList() }
        val argvCountAddr: WasmPtr = 0x80
        val argvBufSizeAddr: WasmPtr = 0x100

        val code = argsSizesGetHandle.execute(memory, argvCountAddr, argvBufSizeAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory).byteAt(argvCountAddr).isEqualTo(0)
        assertThat(memory).byteAt(argvBufSizeAddr).isEqualTo(0)
    }

    @Test
    fun argsGet_test_single_empty_argument() {
        host.commandArgsProvider = CommandArgsProvider { listOf("") }
        val argvCountAddr: WasmPtr = 0x80
        val argvBufSizeAddr: WasmPtr = 0x100

        val code = argsSizesGetHandle.execute(memory, argvCountAddr, argvBufSizeAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory.readI32(argvCountAddr)).isEqualTo(1)
        assertThat(memory).hasBytesAt(argvBufSizeAddr, byteArrayOf(1))
    }
}
