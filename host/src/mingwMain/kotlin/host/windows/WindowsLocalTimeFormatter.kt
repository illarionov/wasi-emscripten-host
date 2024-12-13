/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import arrow.core.getOrElse
import at.released.weh.host.LocalTimeFormatter
import at.released.weh.host.LocalTimeFormatter.StructTm
import at.released.weh.host.windows.ext.toKStringFromLocalCodepage
import kotlinx.cinterop.LongVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.__time64_t
import platform.posix._localtime64_s
import platform.posix._tzname
import platform.posix.timezone_
import platform.posix.tm

internal object WindowsLocalTimeFormatter : LocalTimeFormatter {
    override fun format(epochSeconds: Long): StructTm = memScoped {
        val epochRef: LongVarOf<__time64_t> = alloc<LongVarOf<__time64_t>>().apply {
            value = epochSeconds
        }
        val tm: tm = alloc()

        val error = _localtime64_s(tm.ptr, epochRef.ptr)
        if (error != 0) {
            error("_localtime64_s() failed")
        }
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
            tm_gmtoff = readGmtoff().toLong(),
            tm_zone = readLocalTzname(tm.tm_isdst != 0),
        )
    }

    private fun readGmtoff(): Int = -timezone_

    private fun readLocalTzname(isDst: Boolean): String? {
        val index = if (isDst) 1 else 0
        return _tzname[index]?.toKStringFromLocalCodepage()?.getOrElse { null }
    }
}
