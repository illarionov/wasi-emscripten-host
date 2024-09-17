/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.WasmPtrUtil.ptrIsNull
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.readString

@InternalWasiEmscriptenHostApi
public interface ReadOnlyMemory {
    public fun readI8(@IntWasmPtr addr: WasmPtr): Byte
    public fun readI32(@IntWasmPtr addr: WasmPtr): Int
    public fun readI64(@IntWasmPtr addr: WasmPtr): Long

    public fun source(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSource
}

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU8(@IntWasmPtr addr: WasmPtr): UByte = readI8(addr).toUByte()

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU32(@IntWasmPtr addr: WasmPtr): UInt = readI32(addr).toUInt()

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU64(@IntWasmPtr addr: WasmPtr): ULong = readI64(addr).toULong()

@InternalWasiEmscriptenHostApi
@IntWasmPtr
public fun ReadOnlyMemory.readPtr(@IntWasmPtr addr: WasmPtr): WasmPtr = readI32(addr)

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readNullableNullTerminatedString(@IntWasmPtr(ref = Byte::class) offset: WasmPtr): String? {
    return if (!ptrIsNull(offset)) {
        readNullTerminatedString(offset)
    } else {
        null
    }
}

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readNullTerminatedString(@IntWasmPtr(ref = Byte::class) offset: WasmPtr): String {
    check(offset != 0)

    val mem = Buffer()
    var addr = offset
    do {
        val byte = this.readI8(addr)
        addr += 1
        if (byte == 0.toByte()) {
            break
        }
        mem.writeByte(byte)
    } while (true)

    return mem.readString()
}

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.sourceWithMaxSize(
    @IntWasmPtr fromAddr: WasmPtr,
    maxSize: Int,
): RawSource = source(fromAddr, fromAddr + maxSize)
