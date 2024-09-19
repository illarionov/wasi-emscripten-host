/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple

import at.released.weh.host.TimeZoneInfoProvider
import at.released.weh.host.include.TimeZoneInfo

internal class AppleTimeZoneInfoProvider : TimeZoneInfoProvider {
    override fun getTimeZoneInfo(): TimeZoneInfo {
        error("Not implemented")
    }
}
