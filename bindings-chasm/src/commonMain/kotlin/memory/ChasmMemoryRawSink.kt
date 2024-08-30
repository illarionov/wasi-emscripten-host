/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSink
import io.github.charlietap.chasm.embedding.memory.writeMemory
import io.github.charlietap.chasm.executor.runtime.store.Address
import io.github.charlietap.chasm.executor.runtime.store.Store
import kotlinx.io.Buffer

internal class ChasmMemoryRawSink(
    private val store: Store,
    private val memoryAddress: Address.Memory,
    baseAddr: WasmPtr<*>,
    toAddrExclusive: WasmPtr<*>,
) : MemoryRawSink(baseAddr, toAddrExclusive) {
    override fun writeBytesToMemory(source: Buffer, toAddr: WasmPtr<*>, byteCount: Long) {
        for (addr in baseAddr.addr..<(baseAddr.addr + byteCount).toInt()) {
            val byte = source.readByte()
            writeMemory(store, memoryAddress, addr, byte)
        }
    }
}
