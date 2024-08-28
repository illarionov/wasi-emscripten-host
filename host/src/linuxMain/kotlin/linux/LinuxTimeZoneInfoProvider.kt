/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import at.released.weh.host.EmbedderHost.TimeZoneInfoProvider
import at.released.weh.host.include.TimeZoneInfo
import kotlinx.cinterop.get
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.tzname

internal object LinuxTimeZoneInfoProvider : TimeZoneInfoProvider {
    override fun getTimeZoneInfo(): TimeZoneInfo {
        return TimeZoneInfo(
            timeZone = platform.posix.timezone_,
            daylight = platform.posix.daylight,
            stdName = tzname[0]?.toKStringFromUtf8() ?: "unk",
            dstName = tzname[1]?.toKStringFromUtf8() ?: "unk",
        )
    }
}
