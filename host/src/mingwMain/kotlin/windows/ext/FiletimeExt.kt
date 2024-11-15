/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LONG_NUMERICAL_VALUES_SEPARATED")

package at.released.weh.host.windows.ext

import platform.windows.FILETIME

// The number of 100-nanosecond intervals from January 1, 1601 to January 1, 1970
internal const val FILETIME_1970 = 0x019DB1DED53E8000UL

internal val FILETIME.unixTimeNs: Long
    get() {
        val ticksFrom1970 = (dwHighDateTime.toULong().shl(32) or dwLowDateTime.toULong()) - FILETIME_1970
        return ticksFrom1970.toLong() * 100L
    }

internal val FILETIME.elapsedTimeNs: Long
    get() {
        val ticksFrom1970 = (dwHighDateTime.toULong().shl(32) or dwLowDateTime.toULong())
        return ticksFrom1970.toLong() * 100L
    }
