/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.memory

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.MemoryRawSink
import com.dylibso.chicory.runtime.Memory
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

internal class ChicoryMemoryRawSink(
    private val wasmMemory: Memory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSink(baseAddr, toAddrExclusive) {
    override fun writeBytesToMemory(source: Buffer, @IntWasmPtr toAddr: WasmPtr, byteCount: Long) {
        val data = source.readByteArray(byteCount.toInt())
        wasmMemory.write(toAddr, data)
    }
}
