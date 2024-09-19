/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core.memory

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.writeString

public interface Memory : ReadOnlyMemory {
    public fun writeI8(@IntWasmPtr addr: WasmPtr, data: Byte)
    public fun writeI32(@IntWasmPtr addr: WasmPtr, data: Int)
    public fun writeI64(@IntWasmPtr addr: WasmPtr, data: Long)

    public fun sink(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSink
}

public fun Memory.writeU8(@IntWasmPtr addr: WasmPtr, data: UByte): Unit = writeI8(addr, data.toByte())

public fun Memory.writeU32(@IntWasmPtr addr: WasmPtr, data: UInt): Unit = writeI32(addr, data.toInt())

public fun Memory.writeU64(@IntWasmPtr addr: WasmPtr, data: ULong): Unit = writeI64(addr, data.toLong())

public fun Memory.writePtr(@IntWasmPtr addr: WasmPtr, @IntWasmPtr data: WasmPtr): Unit = writeI32(addr, data)

public fun Memory.writeNullTerminatedString(
    @IntWasmPtr offset: WasmPtr,
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

@IntWasmPtr
public fun Memory.sinkWithMaxSize(
    @IntWasmPtr fromAddr: WasmPtr,
    maxSize: Int,
): RawSink = sink(fromAddr, fromAddr + maxSize)
