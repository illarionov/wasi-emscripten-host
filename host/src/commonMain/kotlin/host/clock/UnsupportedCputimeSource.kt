/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.clock

import at.released.weh.host.clock.CputimeSource.CpuClockId
import at.released.weh.host.clock.CputimeSource.CputimeClock

internal object UnsupportedCputimeSource : CputimeSource {
    private object UnsupportedClock : CputimeClock {
        override val isSupported: Boolean = false
        override fun getTimeMarkNanoseconds(): Long = -1
        override fun getResolutionNanoseconds(): Long = -1
    }

    override fun getClock(clock: CpuClockId): CputimeClock = UnsupportedClock
}
