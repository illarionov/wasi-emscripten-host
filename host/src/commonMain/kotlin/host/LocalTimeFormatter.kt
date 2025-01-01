/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.host.LocalTimeFormatter.IsDstFlag

public fun interface LocalTimeFormatter {
    public fun format(epochSeconds: Long): StructTm

    /**
     * Struct tm from <time.h>
     *
     * @param tm_sec seconds (0..60)
     * @param tm_min minutes (0..59)
     * @param tm_hour hour (0..23)
     * @param tm_mday Day of month (1..31)
     * @param tm_mon Month (0..11) 0 - January
     * @param tm_year Year - 1900
     * @param tm_wday Day of week (0..6), 0 - Sunday
     * @param tm_yday Day of year (0..365)
     * @param tm_isdst Daylight savings flag
     * @param tm_gmtoff Seconds East of UTC
     * @param tm_zone Timezone abbreviation
     */
    @WasiEmscriptenHostDataModel
    @Suppress("ConstructorParameterNaming", "MagicNumber")
    public class StructTm(
        public val tm_sec: Int,
        public val tm_min: Int,
        public val tm_hour: Int,
        public val tm_mday: Int,
        public val tm_mon: Int,
        public val tm_year: Int,
        public val tm_wday: Int,
        public val tm_yday: Int,
        public val tm_isdst: Int,
        public val tm_gmtoff: Long,
        public val tm_zone: String? = null,
    ) {
        public val isDstFlag: IsDstFlag = when {
            tm_isdst < 0 -> IsDstFlag.UNKNOWN
            tm_isdst == 0 -> IsDstFlag.NOT_IN_EFFECT
            else -> IsDstFlag.IN_EFFECT
        }

        init {
            check(tm_sec in 0..60)
            check(tm_min in 0..59)
            check(tm_hour in 0..23)
            check(tm_mday in 1..31)
            check(tm_mon in 0..11)
            check(tm_wday in 0..6)
            check(tm_yday in 0..365)
        }
    }

    public enum class IsDstFlag {
        IN_EFFECT,
        NOT_IN_EFFECT,
        UNKNOWN,
    }
}

internal fun IsDstFlag.asTmIsdstValue(): Int = when (this) {
    IsDstFlag.IN_EFFECT -> 1
    IsDstFlag.NOT_IN_EFFECT -> 0
    IsDstFlag.UNKNOWN -> -1
}
