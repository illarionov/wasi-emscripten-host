/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.host.TimeZoneInfo
import at.released.weh.host.TimeZoneInfoProviderTestFixtures.europeParis
import at.released.weh.test.utils.withTimeZone
import kotlin.test.Test

class AppleTimeZoneInfoProviderTest {
    @Test
    fun time_zone_provider_should_work() = withTimeZone(europeParis.timeZoneName) {
        val timeZoneInfo: TimeZoneInfo = AppleTimeZoneInfoProvider.getTimeZoneInfo()
        assertThat(timeZoneInfo).isEqualTo(europeParis.timeZoneInfo)
    }
}
