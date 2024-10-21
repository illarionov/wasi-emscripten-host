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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClockTimeGetFunctionHandleTest {
    private val host = TestEmbedderHost().apply {
        clock = object : Clock {
            override fun getCurrentTimeEpochNanoseconds(): Long = TEST_REAL_TIMESTAMP
            override fun getResolutionNanoseconds(): Long = 10L
        }
        monotonicClock = object : MonotonicClock {
            override fun getTimeMarkNanoseconds(): Long = TEST_MONOTONIC_TIMESTAMP
            override fun getResolutionNanoseconds(): Long = 10L
        }
        cputimeSource = UNSUPPORTED_CPUTIME_SOURCE
    }
    private val memory = TestMemory()
    private val timestampAddr: WasmPtr = 0x80
    private val clockTimeGetFunctionHandle = ClockTimeGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun clockTimeGet_success_case() {
        tableOf("clock", "expectedValue")
            .row(Clockid.REALTIME, TEST_REAL_TIMESTAMP)
            .row(Clockid.MONOTONIC, TEST_MONOTONIC_TIMESTAMP)
            .forAll { clockid: Clockid, expectedTimestamp: Long ->
                val errNo = clockTimeGetFunctionHandle.execute(
                    memory = memory,
                    id = clockid.code,
                    precision = 1.milliseconds.inWholeNanoseconds,
                    timestampAddr = timestampAddr,
                )
                assertThat(errNo).isEqualTo(SUCCESS)
                assertThat(memory.readU64(timestampAddr)).isEqualTo(expectedTimestamp.toULong())
            }
    }

    @Test
    fun clockTimeGet_should_return_inval_on_incorrect_code() {
        val timestampAddr: WasmPtr = 0x80
        val errNo = clockTimeGetFunctionHandle.execute(
            memory = memory,
            id = 42,
            precision = 1.milliseconds.inWholeNanoseconds,
            timestampAddr = timestampAddr,
        )
        assertThat(errNo).isEqualTo(INVAL)
    }

    @Test
    fun clockTimeGet_should_return_notsup_on_unsupported_clock() {
        val errNo = clockTimeGetFunctionHandle.execute(
            memory = memory,
            id = PROCESS_CPUTIME_ID.code,
            precision = 10L,
            timestampAddr = timestampAddr,
        )
        assertThat(errNo).isEqualTo(NOTSUP)
    }

    // Ignoring precision because tests from WASI test suite use value of 1
    @Ignore
    @Test
    fun clockTimeGet_should_return_notsup_on_no_enough_precision() {
        val errNo = clockTimeGetFunctionHandle.execute(
            memory = memory,
            id = Clockid.REALTIME.code,
            precision = 9L,
            timestampAddr = timestampAddr,
        )
        assertThat(errNo).isEqualTo(NOTSUP)
    }

    companion object {
        const val TEST_MONOTONIC_TIMESTAMP: Long = 424L
        val TEST_REAL_TIMESTAMP: Long = 1_729_539_007.seconds.inWholeNanoseconds
    }
}
