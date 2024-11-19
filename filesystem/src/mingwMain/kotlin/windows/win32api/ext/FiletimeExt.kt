/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LONG_NUMERICAL_VALUES_SEPARATED")

package at.released.weh.filesystem.windows.win32api.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.op.stat.StructTimespec
import platform.windows.FILETIME
import platform.windows.LARGE_INTEGER

// The number of 100-nanosecond intervals from January 1, 1601 to January 1, 1970
internal const val FILETIME_1970 = 0x019DB1DED53E8000UL

@InternalWasiEmscriptenHostApi
public val FILETIME.unixTimeNs: Long
    get() {
        val ticksFrom1601 = (dwHighDateTime.toULong().shl(32) or dwLowDateTime.toULong())
        val ticksFrom1970 = ticksFrom1601ToTicksFrom1970(ticksFrom1601)
        return ticksFrom1970.toLong() * 100L
    }

@InternalWasiEmscriptenHostApi
public val FILETIME.elapsedTimeNs: Long
    get() {
        val ticks = (dwHighDateTime.toULong().shl(32) or dwLowDateTime.toULong())
        return ticks.toLong() * 100L
    }

internal val FILETIME.asStructTimespec: StructTimespec
    get() {
        val ticksFrom1601 = (dwHighDateTime.toULong().shl(32) or dwLowDateTime.toULong())
        return ticksFrom1601ToStructTimespec(ticksFrom1601)
    }

internal val LARGE_INTEGER.asStructTimespec: StructTimespec
    get() = ticksFrom1601ToStructTimespec(QuadPart.toULong())

private fun ticksFrom1601ToStructTimespec(
    ticksFrom1601: ULong
): StructTimespec {
    val ticksFrom1970 = ticksFrom1601ToTicksFrom1970(ticksFrom1601)
    return StructTimespec(
        seconds = (ticksFrom1970 / 10_000_000UL).toLong(),
        nanoseconds = 100 * (ticksFrom1970 % 10_000_000UL).toLong(),
    )
}

private fun ticksFrom1601ToTicksFrom1970(
    ticksFrom1601: ULong
): ULong = if (ticksFrom1601 >= FILETIME_1970) {
    ticksFrom1601 - FILETIME_1970
} else {
    0UL
}
