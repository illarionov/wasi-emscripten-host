/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.memory

import at.released.weh.host.base.memory.MemoryRawSink
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.Buffer
import kotlinx.io.asInputStream
import org.graalvm.wasm.memory.WasmMemory

internal class GraalvmMemoryRawSink(
    private val memoryProvider: () -> WasmMemory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSink(baseAddr, toAddrExclusive) {
    override fun writeBytesToMemory(source: Buffer, @IntWasmPtr toAddr: WasmPtr, byteCount: Long) {
        val inputStream = source.asInputStream()
        val bytesWritten = memoryProvider().copyFromStream(null, inputStream, toAddr, byteCount.toInt())
        check(bytesWritten >= 0) {
            "End of the stream has been reached"
        }
    }
}
