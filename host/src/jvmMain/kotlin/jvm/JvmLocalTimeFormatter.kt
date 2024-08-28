/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.jvm

import at.released.weh.host.EmbedderHost.LocalTimeFormatter
import at.released.weh.host.include.StructTm
import at.released.weh.host.include.asTmIsdstValue
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

public class JvmLocalTimeFormatter(
    public val localTimeZoneProvider: () -> ZoneId = ZoneId::systemDefault,
) : LocalTimeFormatter {
    override fun format(epochSeconds: Long): StructTm {
        val instant = Instant.ofEpochSecond(epochSeconds)

        val date: ZonedDateTime = ZonedDateTime.ofInstant(
            instant,
            localTimeZoneProvider(),
        )
        val zone = date.zone
        @Suppress("COMPLEX_EXPRESSION")
        return StructTm(
            tm_sec = date.second,
            tm_min = date.minute,
            tm_hour = date.hour,
            tm_mday = date.dayOfMonth,
            tm_mon = date.monthValue - 1,
            tm_year = date.year - 1900,
            tm_wday = date.dayOfWeek.value % 7,
            tm_yday = date.dayOfYear - 1,
            tm_isdst = if (date.zone.rules.isDaylightSavings(date.toInstant())) {
                StructTm.IsDstFlag.IN_EFFECT
            } else {
                StructTm.IsDstFlag.NOT_IN_EFFECT
            }.asTmIsdstValue(),
            tm_gmtoff = zone.rules.getOffset(date.toInstant()).totalSeconds.toLong(),
            tm_zone = zone.id,
        )
    }
}
