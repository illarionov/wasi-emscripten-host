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

@InternalWasiEmscriptenHostApi
public fun ByteString.readU64Le(addr: Int): ULong = (this[addr].toULong() and 0xffUL) or
        (this[addr + 1].toULong() and 0xffUL shl 8) or
        (this[addr + 2].toULong() and 0xffUL shl 16) or
        (this[addr + 3].toULong() and 0xffUL shl 24) or
        (this[addr + 4].toULong() and 0xffUL shl 32) or
        (this[addr + 5].toULong() and 0xffUL shl 40) or
        (this[addr + 6].toULong() and 0xffUL shl 48) or
        (this[addr + 7].toULong() and 0xffUL shl 56)

@InternalWasiEmscriptenHostApi
public fun ByteString.readU32Le(addr: Int): UInt = (this[addr].toUInt() and 0xffU) or
        (this[addr + 1].toUInt() and 0xffU shl 8) or
        (this[addr + 2].toUInt() and 0xffU shl 16) or
        (this[addr + 3].toUInt() and 0xffU shl 24)
