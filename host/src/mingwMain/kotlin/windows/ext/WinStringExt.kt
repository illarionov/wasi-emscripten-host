/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows.ext

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.get

/**
 * Reads zero-terminated opaque sequence of WCHARs starting from [this]
 */
internal fun CPointer<UShortVar>.readNullTerminatedCharArray(): CharArray {
    var length = 0
    val ptr = this
    while (ptr[length] != 0.toUShort()) {
        length += 1
    }
    return CharArray(length) { idx ->
        ptr[idx].toInt().toChar()
    }
}
