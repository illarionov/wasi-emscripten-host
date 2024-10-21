/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux.clock

import assertk.assertThat
import assertk.assertions.isBetween
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

@Suppress("MagicNumber")
class LinuxClockTest {
    private val clock = LinuxClock

    @Test
    fun test_clock_time() {
        val now = Clock.System.now().toEpochMilliseconds()

        val epoch = clock.getCurrentTimeEpochNanoseconds().nanoseconds.inWholeMilliseconds
        assertThat(epoch).isBetween(now, now + 50)
    }

    @Test
    @Suppress("MagicNumber")
    fun test_clock_time_resolution() {
        val resolution = clock.getResolutionNanoseconds()
        assertThat(resolution).isBetween(1, 100)
    }
}
