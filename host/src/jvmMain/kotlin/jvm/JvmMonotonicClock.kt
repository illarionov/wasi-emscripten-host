/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.jvm

import at.released.weh.host.clock.MonotonicClock
import kotlin.time.Duration.Companion.milliseconds

internal object JvmMonotonicClock : MonotonicClock {
    override fun getTimeMarkNanoseconds(): Long = System.nanoTime()
    override fun getResolutionNanoseconds(): Long {
        // XXX: need precise resolution
        return 1.milliseconds.inWholeNanoseconds
    }
}
