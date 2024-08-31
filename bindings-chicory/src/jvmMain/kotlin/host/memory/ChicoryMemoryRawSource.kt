/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.memory

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSource
import com.dylibso.chicory.runtime.Memory
import com.dylibso.chicory.runtime.exceptions.WASMRuntimeException
import kotlinx.io.Buffer

internal class ChicoryMemoryRawSource(
    private val wasmMemory: Memory,
    baseAddr: WasmPtr<*>,
    toAddrExclusive: WasmPtr<*>,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(srcAddr: WasmPtr<*>, sink: Buffer, readBytes: Int) {
        try {
            val bytes = wasmMemory.readBytes(srcAddr.addr, readBytes)
            sink.write(bytes)
        } catch (oob: WASMRuntimeException) {
            throw IllegalStateException("Out of bounds memory access", oob)
        } finally {
            sink.emit()
        }
    }
}
