/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.LocalTimeFormatter.StructTm
import at.released.weh.host.LocalTimeFormatterTestFixtures
import at.released.weh.test.utils.withTimeZone
import kotlin.test.Test

class WindowsLocalTimeFormatterTest {
    @Test
    fun formatter_should_work() = LocalTimeFormatterTestFixtures.novosibirskJul27.let { test ->
        withTimeZone("GMT-07") {
            val tm: StructTm = WindowsLocalTimeFormatter.format(test.timeEpochSeconds)
            assertThat(tm).isEqualTo(test.timeInfo.withTmZone(tm_zone = tm.tm_zone))
        }
    }

    @Suppress("FunctionParameterNaming")
    private fun StructTm.withTmZone(tm_zone: String?): StructTm = StructTm(
        tm_sec = tm_sec,
        tm_min = tm_min,
        tm_hour = tm_hour,
        tm_mday = tm_mday,
        tm_mon = tm_mon,
        tm_year = tm_year,
        tm_wday = tm_wday,
        tm_yday = tm_yday,
        tm_isdst = tm_isdst,
        tm_gmtoff = tm_gmtoff,
        tm_zone = tm_zone,
    )
}
