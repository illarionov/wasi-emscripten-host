/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.test.fixtures.TestCputimeSource.Companion.UNSUPPORTED_CPUTIME_SOURCE
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Clockid
import at.released.weh.wasi.preview1.type.Clockid.PROCESS_CPUTIME_ID
import at.released.weh.wasi.preview1.type.Errno.INVAL
import at.released.weh.wasi.preview1.type.Errno.NOTSUP
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.readU64
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClockResGetFunctionHandleTest {
    private val host = TestEmbedderHost().apply {
        clock = object : Clock {
            override fun getCurrentTimeEpochNanoseconds(): Long = 1L
            override fun getResolutionNanoseconds(): Long = 172953L
        }
        monotonicClock = object : MonotonicClock {
            override fun getTimeMarkNanoseconds(): Long = 1L
            override fun getResolutionNanoseconds(): Long = 172955L
        }
        cputimeSource = UNSUPPORTED_CPUTIME_SOURCE
    }
    private val memory = TestMemory()
    private val timestampAddr: WasmPtr = 0x80
    private val clockGetResFunctionHandle = ClockResGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun clockGetRes_success_case() {
        tableOf("clock", "expectedValue")
            .row(Clockid.REALTIME, 172953UL)
            .row(Clockid.MONOTONIC, 172955UL)
            .forAll { clockid: Clockid, expectedTimestamp: ULong ->
                val errrNo = clockGetResFunctionHandle.execute(memory, clockid.code, timestampAddr)
                assertThat(errrNo).isEqualTo(SUCCESS)
                assertThat(memory.readU64(timestampAddr)).isEqualTo(expectedTimestamp)
            }
    }

    @Test
    fun clockGetRes_should_return_inval_on_incorrect_code() {
        val timestampAddr: WasmPtr = 0x80
        val errNo = clockGetResFunctionHandle.execute(memory, 42, timestampAddr)
        assertThat(errNo).isEqualTo(INVAL)
    }

    @Test
    fun clockGetRes_should_return_notsup_on_unsupported_clock() {
        val errNo = clockGetResFunctionHandle.execute(memory, PROCESS_CPUTIME_ID.code, timestampAddr)
        assertThat(errNo).isEqualTo(NOTSUP)
    }
}
