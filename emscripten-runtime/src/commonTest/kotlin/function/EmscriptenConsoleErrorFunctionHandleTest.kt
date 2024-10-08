/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.emcripten.runtime.function

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.test.logger.BaseLogger
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.writeNullTerminatedString
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmscriptenConsoleErrorFunctionHandleTest {
    private val host = TestEmbedderHost()
    private val memory = TestMemory()

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun consoleErrorLog_success_case() {
        var errorLogged = false
        var loggedThrowable: Throwable? = null
        var loggedMessage: String? = null
        host.rootLogger = object : BaseLogger() {
            override fun e(throwable: Throwable?, message: () -> String) {
                errorLogged = true
                loggedThrowable = throwable
                loggedMessage = message()
            }
        }
        val handle = EmscriptenConsoleErrorFunctionHandle(host)

        val testMessage = "Test message"

        @IntWasmPtr
        val messagePtr: WasmPtr = 128

        memory.writeNullTerminatedString(messagePtr, testMessage)

        handle.execute(memory, messagePtr)

        assertThat(errorLogged).isTrue()
        assertThat(loggedThrowable).isNull()
        assertThat(loggedMessage).isEqualTo(testMessage)
    }
}
