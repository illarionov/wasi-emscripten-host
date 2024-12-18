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
            assertThat(tm).isEqualTo(test.timeInfo.copy(tm_zone = tm.tm_zone))
        }
    }
}