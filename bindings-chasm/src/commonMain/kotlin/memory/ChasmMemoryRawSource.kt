/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSource
import io.github.charlietap.chasm.embedding.error.ChasmError.ExecutionError
import io.github.charlietap.chasm.embedding.memory.readBytes
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.fold
import kotlinx.io.Buffer

internal class ChasmMemoryRawSource(
    private val store: Store,
    private val memory: Memory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(@IntWasmPtr srcAddr: WasmPtr, sink: Buffer, readBytes: Int) {
        val buffer = ByteArray(readBytes)
        readBytes(store, memory, buffer, srcAddr, readBytes).fold(
            onSuccess = { bytes ->
                sink.write(bytes)
                sink.emit()
            },
            onError = { executionError: ExecutionError ->
                throw IllegalStateException("Read from memory failed: ${executionError.error}")
            },
        )
    }
}
