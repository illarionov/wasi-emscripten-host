/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple.clock

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.clock_getres
import platform.posix.clock_gettime_nsec_np
import platform.posix.clockid_t
import platform.posix.timespec
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

internal fun getTime(clockId: clockid_t): Long = memScoped {
    return clock_gettime_nsec_np(clockId).toLong()
}

internal fun getTimerResolution(clockId: clockid_t): Long = memScoped {
    val timespec: timespec = alloc()
    if (clock_getres(clockId, timespec.ptr) != -1) {
        (timespec.tv_sec.seconds + timespec.tv_nsec.nanoseconds).inWholeNanoseconds
    } else {
        -1L
    }
}
