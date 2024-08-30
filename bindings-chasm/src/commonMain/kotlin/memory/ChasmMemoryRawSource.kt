/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSource
import io.github.charlietap.chasm.ChasmResult.Error
import io.github.charlietap.chasm.ChasmResult.Success
import io.github.charlietap.chasm.embedding.memory.readMemory
import io.github.charlietap.chasm.executor.runtime.store.Address
import io.github.charlietap.chasm.executor.runtime.store.Store
import kotlinx.io.Buffer

internal class ChasmMemoryRawSource(
    private val store: Store,
    private val memoryAddress: Address.Memory,
    baseAddr: WasmPtr<*>,
    toAddrExclusive: WasmPtr<*>,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(srcAddr: WasmPtr<*>, sink: Buffer, readBytes: Int) {
        try {
            for (addr in srcAddr.addr..<srcAddr.addr + readBytes) {
                @Suppress("UseCheckOrError")
                when (val readResult = readMemory(store, memoryAddress, addr)) {
                    is Success -> sink.writeByte(readResult.result)
                    is Error -> throw IllegalStateException("Read from memory failed: ${readResult.error.error}")
                }
            }
        } finally {
            sink.emit()
        }
    }
}
