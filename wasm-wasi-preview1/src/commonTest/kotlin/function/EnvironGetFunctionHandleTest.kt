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
import at.released.weh.wasm.core.test.assertions.hasBytesAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import at.released.weh.wasm.core.test.fixtures.TestMemory.Companion.MEMORY_FILL_BYTE
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EnvironGetFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()
    private val envGetFunctionHandle = EnvironGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun envGet_success_case() {
        val testEnv = mapOf(
            "ARG1" to "VAL1",
            "A\" RG2 \"" to "VAL 2",
            "ARG=3" to "VAL==3",
            "" to "VAL 4",
            "ARG5" to "",
        )

        host.systemEnvProvider = SystemEnvProvider { testEnv }
        val environ: WasmPtr = 0x80
        val environBuf: WasmPtr = 0x100
        val code = envGetFunctionHandle.execute(memory, environ, environBuf)

        assertThat(code).isEqualTo(SUCCESS)

        assertThat(memory).hasBytesAt(
            memory.readI32(environ + 0),
            "ARG1=VAL1".encodeToByteArray() + 0.toByte(),
        )
        assertThat(memory).hasBytesAt(
            memory.readI32(environ + 4),
            "A\" RG2 \"=VAL 2".encodeToByteArray() + 0.toByte(),
        )
        assertThat(memory).hasBytesAt(
            memory.readI32(environ + 8),
            "ARG3=VAL==3".encodeToByteArray() + 0.toByte(),
        )
        assertThat(memory).hasBytesAt(
            memory.readI32(environ + 12),
            "=VAL 4".encodeToByteArray() + 0.toByte(),
        )
        assertThat(memory).hasBytesAt(
            memory.readI32(environ + 16),
            "ARG5=".encodeToByteArray() + 0.toByte(),
        )
    }

    @Test
    fun envGet_empty_list() {
        host.systemEnvProvider = SystemEnvProvider { emptyMap() }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100

        val code = envGetFunctionHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory).byteAt(argvAddr).isEqualTo(MEMORY_FILL_BYTE)
        assertThat(memory).byteAt(argvBufAddr).isEqualTo(MEMORY_FILL_BYTE)
    }

    @Test
    fun envGet_test_single_empty_argument() {
        host.systemEnvProvider = SystemEnvProvider { mapOf("" to "") }
        val argvAddr: WasmPtr = 0x80
        val argvBufAddr: WasmPtr = 0x100

        val code = envGetFunctionHandle.execute(memory, argvAddr, argvBufAddr)

        assertThat(code).isEqualTo(SUCCESS)
        assertThat(memory.readI32(argvAddr)).isEqualTo(argvBufAddr)
        assertThat(memory).hasBytesAt(argvBufAddr, byteArrayOf('='.code.toByte(), 0))
    }
}
