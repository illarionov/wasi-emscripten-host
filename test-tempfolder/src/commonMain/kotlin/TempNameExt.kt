/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlin.random.Random

private const val MAX_RANDOM_NAME_NUMBER = 36L * 36L * 36L * 36L * 36L * 36L

@Suppress("MagicNumber")
internal fun generateTempDirectoryName(prefix: String): String {
    return prefix + Random.nextLong(until = MAX_RANDOM_NAME_NUMBER).toString(36).padStart(6, '0')
}
