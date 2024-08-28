/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.host.base.WasmPtr
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.writeString

@InternalWasiEmscriptenHostApi
public interface Memory : ReadOnlyMemory {
    public fun writeI8(addr: WasmPtr<*>, data: Byte)
    public fun writeI32(addr: WasmPtr<*>, data: Int)
    public fun writeI64(addr: WasmPtr<*>, data: Long)

    public fun sink(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSink
}

@InternalWasiEmscriptenHostApi
public fun Memory.writeU8(addr: WasmPtr<*>, data: UByte): Unit = writeI8(addr, data.toByte())

@InternalWasiEmscriptenHostApi
public fun Memory.writeU32(addr: WasmPtr<*>, data: UInt): Unit = writeI32(addr, data.toInt())

@InternalWasiEmscriptenHostApi
public fun Memory.writeU64(addr: WasmPtr<*>, data: ULong): Unit = writeI64(addr, data.toLong())

@InternalWasiEmscriptenHostApi
public fun Memory.writePtr(addr: WasmPtr<*>, data: WasmPtr<*>): Unit = writeI32(addr, data.addr)

@InternalWasiEmscriptenHostApi
public fun Memory.writeNullTerminatedString(
    offset: WasmPtr<*>,
    value: String,
): Int {
    val buffer = Buffer().apply {
        writeString(value)
        writeByte(0)
    }
    val size = buffer.size.toInt()
    sinkWithMaxSize(offset, size).use {
        it.write(buffer, size.toLong())
    }
    return size
}

@InternalWasiEmscriptenHostApi
public fun <P : Any?> Memory.sinkWithMaxSize(
    fromAddr: WasmPtr<P>,
    maxSize: Int,
): RawSink = sink(fromAddr, WasmPtr<P>(fromAddr.addr + maxSize))
