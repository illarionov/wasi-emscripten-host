/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.include.StructTm
import at.released.weh.test.utils.withTimeZone
import kotlin.test.Test

class LinuxLocalTimeFormatterTest {
    @Test
    @Suppress("MagicNumber")
    fun formatter_should_work() = withTimeZone("Asia/Novosibirsk") {
        val tm: StructTm = LinuxLocalTimeFormatter.format(1_724_702_567)
        assertThat(tm).isEqualTo(
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
    }
}
