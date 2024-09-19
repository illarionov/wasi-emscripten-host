/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.memory

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import com.dylibso.chicory.runtime.Memory as ChicoryMemory

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
public class ChicoryMemoryAdapter(
    internal val wasmMemory: ChicoryMemory,
) : Memory {
    override fun readI8(@IntWasmPtr addr: WasmPtr): Byte {
        return wasmMemory.read(addr)
    }

    override fun readI32(@IntWasmPtr addr: WasmPtr): Int {
        return wasmMemory.readI32(addr).asInt()
    }

    override fun readI64(@IntWasmPtr addr: WasmPtr): Long {
        return wasmMemory.readI64(addr).asLong()
    }

    override fun source(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSource {
        return ChicoryMemoryRawSource(wasmMemory, fromAddr, toAddrExclusive)
    }

    override fun writeI8(@IntWasmPtr addr: WasmPtr, data: Byte) {
        wasmMemory.writeByte(addr, data)
    }

    override fun writeI32(@IntWasmPtr addr: WasmPtr, data: Int) {
        wasmMemory.writeI32(addr, data)
    }

    override fun writeI64(@IntWasmPtr addr: WasmPtr, data: Long) {
        wasmMemory.writeLong(addr, data)
    }

    override fun sink(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSink {
        return ChicoryMemoryRawSink(wasmMemory, fromAddr, toAddrExclusive)
    }
}
