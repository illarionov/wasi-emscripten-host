/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.internal

import at.released.weh.host.EmbedderHost.Clock
import kotlinx.datetime.Clock as KotlinxClock

internal class CommonClock(
    private val clock: KotlinxClock = KotlinxClock.System,
) : Clock {
    override fun getCurrentTimeEpochMilliseconds(): Long = clock.now().toEpochMilliseconds()
}
