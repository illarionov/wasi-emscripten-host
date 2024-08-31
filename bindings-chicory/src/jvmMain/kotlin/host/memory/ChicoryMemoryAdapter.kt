/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.memory

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.Memory
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import com.dylibso.chicory.runtime.Memory as ChicoryMemory

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
internal class ChicoryMemoryAdapter(
    internal val wasmMemory: ChicoryMemory,
) : Memory {
    override fun readI8(addr: WasmPtr<*>): Byte {
        return wasmMemory.read(addr.addr)
    }

    override fun readI32(addr: WasmPtr<*>): Int {
        return wasmMemory.readI32(addr.addr).asInt()
    }

    override fun readI64(addr: WasmPtr<*>): Long {
        return wasmMemory.readI64(addr.addr).asLong()
    }

    override fun source(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSource {
        return ChicoryMemoryRawSource(wasmMemory, fromAddr, toAddrExclusive)
    }

    override fun writeI8(addr: WasmPtr<*>, data: Byte) {
        wasmMemory.writeByte(addr.addr, data)
    }

    override fun writeI32(addr: WasmPtr<*>, data: Int) {
        wasmMemory.writeI32(addr.addr, data)
    }

    override fun writeI64(addr: WasmPtr<*>, data: Long) {
        wasmMemory.writeLong(addr.addr, data)
    }

    override fun sink(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSink {
        return ChicoryMemoryRawSink(wasmMemory, fromAddr, toAddrExclusive)
    }
}
