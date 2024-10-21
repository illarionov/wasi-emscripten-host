/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux.clock

import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.CputimeSource.CpuClockId
import at.released.weh.host.clock.CputimeSource.CpuClockId.PROCESS_CPUTIME
import at.released.weh.host.clock.CputimeSource.CpuClockId.THREAD_CPUTIME
import at.released.weh.host.clock.CputimeSource.CputimeClock
import platform.posix.CLOCK_PROCESS_CPUTIME_ID
import platform.posix.CLOCK_THREAD_CPUTIME_ID
import platform.posix.clock_getres
import platform.posix.clockid_t

internal object LinuxCputimeSource : CputimeSource {
    private val processCputimeClock = LinuxCputimeClock(CLOCK_PROCESS_CPUTIME_ID)
    private val threadCputimeClock = LinuxCputimeClock(CLOCK_THREAD_CPUTIME_ID)

    override fun getClock(clock: CpuClockId): CputimeClock = when (clock) {
        PROCESS_CPUTIME -> processCputimeClock
        THREAD_CPUTIME -> threadCputimeClock
    }

    private class LinuxCputimeClock(
        private val clockId: clockid_t,
    ) : CputimeClock {
        private val _resolution = getTimerResolution(clockId)
        override val isSupported: Boolean = clock_getres(clockId, null) == 0
        override fun getTimeMarkNanoseconds(): Long = getTime(clockId)
        override fun getResolutionNanoseconds(): Long = _resolution
    }
}
