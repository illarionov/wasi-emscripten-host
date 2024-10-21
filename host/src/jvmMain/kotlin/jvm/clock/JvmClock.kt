/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.jvm.clock

import at.released.weh.host.clock.Clock
import kotlin.time.Duration.Companion.milliseconds

internal object JvmClock : Clock {
    @Suppress("MagicNumber")
    override fun getCurrentTimeEpochNanoseconds(): Long = System.currentTimeMillis() * 1_000_000
    override fun getResolutionNanoseconds(): Long {
        // XXX: need precise resolution
        return 1.milliseconds.inWholeNanoseconds
    }
}
