/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import at.released.weh.host.EmbedderHost.LocalTimeFormatter
import at.released.weh.host.include.StructTm
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.LongVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import platform.posix.localtime_r
import platform.posix.time_t
import platform.posix.tm

internal object LinuxLocalTimeFormatter : LocalTimeFormatter {
    override fun format(epochSeconds: Long): StructTm = memScoped {
        val epochRef: LongVarOf<time_t> = alloc<LongVarOf<time_t>>().apply {
            value = epochSeconds
        }
        val resultBuf: tm = alloc()

        val result: CPointer<tm> = localtime_r(epochRef.ptr, resultBuf.ptr) ?: error("localtime_r() failed")
        val tm = result.pointed
        StructTm(
            tm_sec = tm.tm_sec,
            tm_min = tm.tm_min,
            tm_hour = tm.tm_hour,
            tm_mday = tm.tm_mday,
            tm_mon = tm.tm_mon,
            tm_year = tm.tm_year,
            tm_wday = tm.tm_wday,
            tm_yday = tm.tm_yday,
            tm_isdst = tm.tm_isdst,
            tm_gmtoff = tm.tm_gmtoff,
            tm_zone = tm.tm_zone?.toKStringFromUtf8(),
        )
    }
}
