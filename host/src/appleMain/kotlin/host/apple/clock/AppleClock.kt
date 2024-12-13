/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple.clock

import at.released.weh.host.clock.Clock
import platform.posix.CLOCK_REALTIME

internal object AppleClock : Clock {
    private val _resolution = getTimerResolution(CLOCK_REALTIME.toUInt())
    override fun getCurrentTimeEpochNanoseconds(): Long = getTime(CLOCK_REALTIME.toUInt())
    override fun getResolutionNanoseconds(): Long = _resolution
}
