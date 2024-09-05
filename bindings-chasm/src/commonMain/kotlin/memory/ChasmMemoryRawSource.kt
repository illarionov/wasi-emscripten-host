/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

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
    baseAddr: WasmPtr<*>,
    toAddrExclusive: WasmPtr<*>,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(srcAddr: WasmPtr<*>, sink: Buffer, readBytes: Int) {
        readBytes(store, memory, srcAddr.addr, readBytes).fold(
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
