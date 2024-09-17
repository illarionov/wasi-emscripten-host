/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSink
import io.github.charlietap.chasm.embedding.memory.writeBytes
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.onError
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

internal class ChasmMemoryRawSink(
    private val store: Store,
    private val memoryAddress: Memory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSink(baseAddr, toAddrExclusive) {
    override fun writeBytesToMemory(source: Buffer, toAddr: WasmPtr, byteCount: Long) {
        val bytes = source.readByteArray(byteCount.toInt())
        writeBytes(store, memoryAddress, toAddr, bytes).onError { executionError ->
            throw IllegalStateException("Write to memory failed: ${executionError.error}")
        }
    }
}
