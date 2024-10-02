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
import at.released.weh.wasm.core.test.fixtures.TestMemory.Companion.MEMORY_FILL_BYTE
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ArgsGetFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()
    private val argsGetHandle = ArgsGetFunctionHandle(host)

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
        val testArgOffsets = listOf(
            0 to 0, // argv0
            4 to 9, // argv1
            8 to 14, // argv2
            12 to 52, // argv3
        )

        host.commandArgsProvider = CommandArgsProvider { testArgs }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100
        val code = argsGetHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)

        testArgOffsets.forEach { (argvOffset, bufOffsset) ->
            assertThat(memory.readI32(argvAddr + argvOffset))
                .isEqualTo(argvBufAddr + bufOffsset)
        }

        testArgs.forEachIndexed { index, testArg ->
            assertThat(memory).hasBytesAt(
                argvBufAddr + testArgOffsets[index].second,
                testArg.encodeToByteArray() + 0.toByte(),
            )
        }
    }

    @Test
    fun argsGet_test_empty_list() {
        host.commandArgsProvider = CommandArgsProvider { emptyList() }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100

        val code = argsGetHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory).byteAt(argvAddr).isEqualTo(MEMORY_FILL_BYTE)
        assertThat(memory).byteAt(argvBufAddr).isEqualTo(MEMORY_FILL_BYTE)
    }

    @Test
    fun argsGet_test_single_empty_argument() {
        host.commandArgsProvider = CommandArgsProvider { listOf("") }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100

        val code = argsGetHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory.readI32(argvAddr)).isEqualTo(argvBufAddr)
        assertThat(memory).hasBytesAt(argvBufAddr, byteArrayOf(0))
    }

    @Test
    fun argsGet_test_should_remove_zero_byte() {
        host.commandArgsProvider = CommandArgsProvider { listOf("test\u0000 \u0000string") }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100

        val code = argsGetHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory.readI32(argvAddr)).isEqualTo(argvBufAddr)
        assertThat(memory).hasBytesAt(
            argvBufAddr,
            "test string".encodeToByteArray() + 0.toByte(),
        )
    }
}
