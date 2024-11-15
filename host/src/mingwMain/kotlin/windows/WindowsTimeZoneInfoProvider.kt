/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import arrow.core.getOrElse
import at.released.weh.host.TimeZoneInfo
import at.released.weh.host.windows.ext.toKStringFromLocalCodepage
import kotlinx.cinterop.get
import platform.posix._tzname
import platform.posix.daylight
import platform.posix.timezone_

internal object WindowsTimeZoneInfoProvider : TimeZoneInfo.Provider {
    override fun getTimeZoneInfo(): TimeZoneInfo {
        return TimeZoneInfo(
            timeZone = timezone_.toLong(),
            daylight = daylight,
            stdName = _tzname[0]?.toKStringFromLocalCodepage()?.getOrElse { null } ?: "unk",
            dstName = _tzname[1]?.toKStringFromLocalCodepage()?.getOrElse { null } ?: "unk",
        )
    }
}
