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
    maskProperties: List<KProperty0<UInt>>,
    initialFlagsSet: List<String> = emptyList(),
): String {
    var left = mask
    val names = initialFlagsSet.toMutableList()
    maskProperties.forEach { prop: KProperty0<UInt> ->
        val propMask: UInt = prop.get()
        if (left.and(propMask) != 0U) {
            names.add(prop.name)
            left = left.and(propMask.inv())
        }
    }
    return buildString {
        names.joinTo(this, ",")
        if (left != 0U) {
            append("0")
            append(left.toString(8))
        }
    }
}
