/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple.clock

import assertk.assertThat
import assertk.assertions.isBetween
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

class AppleMonotonicClockTest {
    private val clock = AppleMonotonicClock

    @Test
    fun test_monotonic_clock_time() {
        val mark1 = clock.getTimeMarkNanoseconds().nanoseconds
        val mark2 = clock.getTimeMarkNanoseconds().nanoseconds
        assertThat(mark2).isBetween(mark1, mark1 + 1.seconds)
    }

    @Test
    @Suppress("MagicNumber")
    fun test_monotonic_clock_time_resolution() {
        val resolution = clock.getResolutionNanoseconds()
        assertThat(resolution).isBetween(1, 1001)
    }
}
