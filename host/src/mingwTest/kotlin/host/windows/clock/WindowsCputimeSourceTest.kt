/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.clock

import assertk.assertThat
import assertk.assertions.isGreaterThan
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isTrue
import at.released.weh.host.clock.CputimeSource.CpuClockId.PROCESS_CPUTIME
import at.released.weh.host.clock.CputimeSource.CpuClockId.THREAD_CPUTIME
import kotlin.test.Test

@Suppress("MagicNumber")
class WindowsCputimeSourceTest {
    @Test
    fun test_cputime_process_time() {
        val processClock = WindowsCputimeSource.getClock(PROCESS_CPUTIME)
        assertThat(processClock.isSupported).isTrue()
        assertThat(processClock.getTimeMarkNanoseconds()).isGreaterThanOrEqualTo(0)
        assertThat(processClock.getResolutionNanoseconds()).isGreaterThan(0L)
    }

    @Test
    fun test_cputime_thread_time() {
        val threadClock = WindowsCputimeSource.getClock(THREAD_CPUTIME)
        assertThat(threadClock.isSupported).isTrue()
        assertThat(threadClock.getTimeMarkNanoseconds()).isGreaterThanOrEqualTo(0)
        assertThat(threadClock.getResolutionNanoseconds()).isGreaterThan(0L)
    }
}
