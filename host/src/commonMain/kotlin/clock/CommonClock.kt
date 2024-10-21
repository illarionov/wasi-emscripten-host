/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.clock

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.datetime.Clock as KotlinxClock

internal class CommonClock(
    private val clock: KotlinxClock = KotlinxClock.System,
) : Clock {
    @Suppress("MagicNumber")
    override fun getCurrentTimeEpochNanoseconds(): Long = clock.now().let {
        it.epochSeconds * 1_000_000_000 + it.nanosecondsOfSecond
    }

    override fun getResolutionNanoseconds(): Long {
        // XXX: need precise resolution
        return 1.milliseconds.inWholeNanoseconds
    }
}
