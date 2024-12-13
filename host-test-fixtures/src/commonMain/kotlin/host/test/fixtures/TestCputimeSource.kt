/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.test.fixtures

import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.CputimeSource.CpuClockId
import at.released.weh.host.clock.CputimeSource.CpuClockId.PROCESS_CPUTIME
import at.released.weh.host.clock.CputimeSource.CpuClockId.THREAD_CPUTIME
import at.released.weh.host.clock.CputimeSource.CputimeClock
import kotlin.time.Duration.Companion.milliseconds

public class TestCputimeSource(
    vararg testClocks: Pair<CpuClockId, CputimeClock> = arrayOf(
        PROCESS_CPUTIME to TestCputimeClock { 2L },
        THREAD_CPUTIME to TestCputimeClock { 1L },
    ),
) : CputimeSource {
    private val testValues: Map<CpuClockId, CputimeClock> = testClocks.toMap()

    override fun getClock(clock: CpuClockId): CputimeClock = testValues[clock] ?: UNSUPPORTED_CLOCK

    public class TestCputimeClock(
        override val isSupported: Boolean = true,
        private val resolution: Long = 1.milliseconds.inWholeNanoseconds,
        private val valueProvider: () -> Long,
    ) : CputimeClock {
        override fun getTimeMarkNanoseconds(): Long {
            return valueProvider()
        }

        override fun getResolutionNanoseconds(): Long {
            return resolution
        }
    }

    public companion object {
        public val UNSUPPORTED_CPUTIME_SOURCE: CputimeSource = TestCputimeSource(testClocks = emptyArray())
        public val UNSUPPORTED_CLOCK: CputimeClock = TestCputimeClock(false, -1L) { -1L }
    }
}
