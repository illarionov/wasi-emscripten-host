/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.clock

import at.released.weh.host.clock.MonotonicClock
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.windows.LARGE_INTEGER
import platform.windows.QueryPerformanceCounter
import platform.windows.QueryPerformanceFrequency

internal object WindowsMonotonicClock : MonotonicClock {
    private val _resolution = getTimerResolutionNs()
    private val counterInstance: LARGE_INTEGER = nativeHeap.alloc()

    override fun getTimeMarkNanoseconds(): Long {
        QueryPerformanceCounter(counterInstance.ptr)
        return counterInstance.QuadPart
    }

    override fun getResolutionNanoseconds(): Long {
        return _resolution
    }

    @Suppress("MagicNumber")
    private fun getTimerResolutionNs(): Long = memScoped {
        val freq: LARGE_INTEGER = alloc()
        if (QueryPerformanceFrequency(freq.ptr) != 0) {
            val hz = freq.QuadPart
            1_000_000_000 / hz
        } else {
            0L
        }
    }
}
