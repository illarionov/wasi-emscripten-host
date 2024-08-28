/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("ConstructorParameterNaming", "MagicNumber")

package at.released.weh.host.include

import at.released.weh.host.include.StructTm.IsDstFlag
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.writeIntLe

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
public data class StructTm(
    val tm_sec: Int,
    val tm_min: Int,
    val tm_hour: Int,
    val tm_mday: Int,
    val tm_mon: Int,
    val tm_year: Int,
    val tm_wday: Int,
    val tm_yday: Int,
    val tm_isdst: Int,
    val tm_gmtoff: Long,
    val tm_zone: String? = null,
) {
    val isDstFlag: IsDstFlag = when {
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

    public enum class IsDstFlag {
        IN_EFFECT,
        NOT_IN_EFFECT,
        UNKNOWN,
    }
}

public fun IsDstFlag.asTmIsdstValue(): Int = when (this) {
    IsDstFlag.IN_EFFECT -> 1
    IsDstFlag.NOT_IN_EFFECT -> 0
    IsDstFlag.UNKNOWN -> -1
}

public const val STRUCT_TM_PACKED_SIZE: Int = 40

public fun StructTm.packTo(sink: Sink): Unit = sink.run {
    writeIntLe(tm_sec) // 0
    writeIntLe(tm_min) // 4
    writeIntLe(tm_hour) // 8
    writeIntLe(tm_mday) // 12
    writeIntLe(tm_mon) // 16
    writeIntLe(tm_year) // 20
    writeIntLe(tm_wday) // 24
    writeIntLe(tm_yday) // 28
    writeIntLe(tm_isdst) // 32
    writeIntLe(tm_gmtoff.toInt()) // 36
}

public fun StructTm.pack(): Buffer = Buffer().also {
    packTo(it)
    check(it.size == STRUCT_TM_PACKED_SIZE.toLong())
}
