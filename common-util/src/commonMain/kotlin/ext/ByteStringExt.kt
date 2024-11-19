/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString

@InternalWasiEmscriptenHostApi
public inline operator fun ByteString.Companion.invoke(size: Int, init: (Int) -> Byte): ByteString {
    return buildByteString(size) {
        repeat(size) {
            append(init(it))
        }
    }
}
