/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.get

/**
 * Reads zero-terminated opaque sequence of WCHARs starting from [this]
 */
@InternalWasiEmscriptenHostApi
public fun CPointer<UShortVar>.readNullTerminatedCharArray(): CharArray {
    var length = 0
    while (this[length] != 0.toUShort()) {
        length += 1
    }
    return this.readChars(length)
}

/**
 * Reads [length] opaque WCHARs starting from [this]
 */
public fun CPointer<UShortVar>.readChars(length: Int): CharArray = CharArray(length) { idx ->
    this[idx].toInt().toChar()
}
