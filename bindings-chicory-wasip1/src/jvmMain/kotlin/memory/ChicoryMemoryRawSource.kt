/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.memory

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.MemoryRawSource
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.WasmRuntimeException
import kotlinx.io.Buffer

internal class ChicoryMemoryRawSource(
    private val wasmMemory: Memory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(@IntWasmPtr srcAddr: WasmPtr, sink: Buffer, readBytes: Int) {
        try {
            val bytes = wasmMemory.readBytes(srcAddr, readBytes)
            sink.write(bytes)
        } catch (oob: WasmRuntimeException) {
            throw IllegalStateException("Out of bounds memory access", oob)
        } finally {
            sink.emit()
        }
    }
}
