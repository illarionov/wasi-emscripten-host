/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

/**
 * Time conversion information from <time.h>:
 *   * extern char \*tzname\[2];
 *   * extern long timezone;
 *   * extern int daylight
 *
 *  @param timeZone Timezone offset, Seconds East of UTC
 *  @param daylight Non-zero if the timezone has daylight saving time rules
 *  @param stdName Timezone name when daylight saving time is not in effect
 *  @param dstName Timezone name when daylight saving time is in effect
 */
public data class TimeZoneInfo(
    val timeZone: Long,
    val daylight: Int,
    val stdName: String,
    val dstName: String,
) {
    public fun interface Provider {
        public fun getTimeZoneInfo(): TimeZoneInfo
    }
}
