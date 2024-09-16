/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlin.reflect.KProperty0

@InternalWasiEmscriptenHostApi
public fun maskToString(
    mask: UInt,
    maskProperties: List<KProperty0<Int>>,
    initialFlagsSet: List<String> = emptyList(),
): String = maskToString(mask.toInt(), maskProperties, initialFlagsSet)

@InternalWasiEmscriptenHostApi
public fun maskToString(
    mask: Int,
    maskProperties: List<KProperty0<Int>>,
    initialFlagsSet: List<String> = emptyList(),
): String {
    var left = mask
    val names = initialFlagsSet.toMutableList()
    maskProperties.forEach { prop: KProperty0<Int> ->
        val propMask: Int = prop.get()
        if (left.and(propMask) != 0) {
            names.add(prop.name)
            left = left.and(propMask.inv())
        }
    }
    return buildString {
        names.joinTo(this, ",")
        if (left != 0) {
            append("0")
            append(left.toString(8))
        }
    }
}
