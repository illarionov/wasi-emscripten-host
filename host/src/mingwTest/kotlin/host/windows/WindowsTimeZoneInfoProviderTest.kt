/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.TimeZoneInfo
import at.released.weh.test.utils.withTimeZone
import kotlin.test.Test

class WindowsTimeZoneInfoProviderTest {
    @Test
    fun time_zone_provider_should_work() = withTimeZone("GMT+03") {
        val timeZoneInfo: TimeZoneInfo = WindowsTimeZoneInfoProvider.getTimeZoneInfo()
        assertThat(timeZoneInfo).isEqualTo(
            TimeZoneInfo(
                timeZone = 10800,
                daylight = 0,
                stdName = "GMT",
                dstName = "",
            ),
        )
    }
}
