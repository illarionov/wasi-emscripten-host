/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.clock

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.CputimeSource.CpuClockId
import at.released.weh.host.clock.CputimeSource.CpuClockId.PROCESS_CPUTIME
import at.released.weh.host.clock.CputimeSource.CpuClockId.THREAD_CPUTIME
import at.released.weh.host.clock.CputimeSource.CputimeClock
import at.released.weh.host.windows.ext.elapsedTimeNs
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.FILETIME
import platform.windows.GetCurrentProcess
import platform.windows.GetCurrentThread
import platform.windows.GetProcessTimes
import platform.windows.GetThreadTimes

internal object WindowsCputimeSource : CputimeSource {
    private object WindowsProcessCputimeClock : CputimeClock {
        override val isSupported: Boolean = getCurrentProcessUserTime().isRight()

        override fun getTimeMarkNanoseconds(): Long = getCurrentProcessUserTime().getOrElse { -1L }
        override fun getResolutionNanoseconds(): Long = 15_625_000

        private fun getCurrentProcessUserTime(): Either<Unit, Long> = memScoped {
            val creationTime: FILETIME = alloc()
            val exitTime: FILETIME = alloc()
            val kernelTime: FILETIME = alloc()
            val userTime: FILETIME = alloc()

            val process = GetCurrentProcess()
            if (GetProcessTimes(process, creationTime.ptr, exitTime.ptr, kernelTime.ptr, userTime.ptr) != 0) {
                userTime.elapsedTimeNs.right()
            } else {
                Unit.left()
            }
        }
    }

    private object WindowsThreadCputimeClock : CputimeClock {
        override val isSupported: Boolean = getCurrentThreadUserTime().isRight()

        override fun getTimeMarkNanoseconds(): Long = getCurrentThreadUserTime().getOrElse { -1L }
        override fun getResolutionNanoseconds(): Long = 15_625_000

        private fun getCurrentThreadUserTime(): Either<Unit, Long> = memScoped {
            val creationTime: FILETIME = alloc()
            val exitTime: FILETIME = alloc()
            val kernelTime: FILETIME = alloc()
            val userTime: FILETIME = alloc()

            if (GetThreadTimes(GetCurrentThread(), creationTime.ptr, exitTime.ptr, kernelTime.ptr, userTime.ptr) != 0) {
                userTime.elapsedTimeNs.right()
            } else {
                Unit.left()
            }
        }
    }

    override fun getClock(clock: CpuClockId): CputimeClock = when (clock) {
        PROCESS_CPUTIME -> WindowsProcessCputimeClock
        THREAD_CPUTIME -> WindowsThreadCputimeClock
    }
}
