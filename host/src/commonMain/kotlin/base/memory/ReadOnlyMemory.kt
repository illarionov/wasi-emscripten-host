/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.isNull
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.readString

@InternalWasiEmscriptenHostApi
public interface ReadOnlyMemory {
    public fun readI8(addr: WasmPtr<*>): Byte
    public fun readI32(addr: WasmPtr<*>): Int
    public fun readI64(addr: WasmPtr<*>): Long

    public fun source(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSource
}

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU8(addr: WasmPtr<*>): UByte = readI8(addr).toUByte()

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU32(addr: WasmPtr<*>): UInt = readI32(addr).toUInt()

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readU64(addr: WasmPtr<*>): ULong = readI64(addr).toULong()

@Suppress("UNCHECKED_CAST")
@InternalWasiEmscriptenHostApi
public fun <T : Any, P : WasmPtr<T>> ReadOnlyMemory.readPtr(addr: WasmPtr<P>): P = WasmPtr<T>(readI32(addr)) as P

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readNullableNullTerminatedString(offset: WasmPtr<Byte>): String? {
    return if (!offset.isNull()) {
        readNullTerminatedString(offset)
    } else {
        null
    }
}

@InternalWasiEmscriptenHostApi
public fun ReadOnlyMemory.readNullTerminatedString(offset: WasmPtr<Byte>): String {
    check(offset.addr != 0)

    val mem = Buffer()
    var addr = offset.addr
    do {
        val byte = this.readI8(WasmPtr<Unit>(addr))
        addr += 1
        if (byte == 0.toByte()) {
            break
        }
        mem.writeByte(byte)
    } while (true)

    return mem.readString()
}

@InternalWasiEmscriptenHostApi
public fun <P : Any?> ReadOnlyMemory.sourceWithMaxSize(
    fromAddr: WasmPtr<P>,
    maxSize: Int,
): RawSource = source(fromAddr, WasmPtr<P>(fromAddr.addr + maxSize))
