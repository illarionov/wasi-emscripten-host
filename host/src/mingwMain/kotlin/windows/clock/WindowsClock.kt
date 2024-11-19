/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.clock

import at.released.weh.filesystem.windows.win32api.ext.unixTimeNs
import at.released.weh.host.clock.Clock
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.FILETIME
import platform.windows.GetSystemTimeAsFileTime

internal object WindowsClock : Clock {
    override fun getCurrentTimeEpochNanoseconds(): Long = memScoped {
        val filetime: FILETIME = alloc()
        GetSystemTimeAsFileTime(filetime.ptr)
        filetime.unixTimeNs
    }

    override fun getResolutionNanoseconds(): Long = 100
}
