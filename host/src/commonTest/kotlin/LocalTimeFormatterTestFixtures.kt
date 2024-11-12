/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.host.LocalTimeFormatter.StructTm

object LocalTimeFormatterTestFixtures {
    val novosibirskJul27 = TimeFormatterTest(
        "Asia/Novosibirsk",
        1_724_702_567,
        StructTm(
            tm_sec = 47,
            tm_min = 2,
            tm_hour = 3,
            tm_mday = 27,
            tm_mon = 7,
            tm_year = 124,
            tm_wday = 2,
            tm_yday = 239,
            tm_isdst = 0,
            tm_gmtoff = 7 * 60 * 60,
            tm_zone = "+07",
        ),
    )

    data class TimeFormatterTest(
        val timeZone: String,
        val timeEpochSeconds: Long,
        val timeInfo: StructTm,
    )
}
