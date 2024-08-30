/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.memory

import at.released.weh.bindings.chasm.exception.ChasmModuleRuntimeErrorException
import at.released.weh.bindings.chasm.ext.orThrow
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.Memory
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrThrow
import io.github.charlietap.chasm.embedding.memory.readMemory
import io.github.charlietap.chasm.embedding.memory.writeMemory
import io.github.charlietap.chasm.executor.memory.grow.MemoryGrowerImpl
import io.github.charlietap.chasm.executor.memory.read.MemoryInstanceIntReaderImpl
import io.github.charlietap.chasm.executor.memory.read.MemoryInstanceLongReaderImpl
import io.github.charlietap.chasm.executor.memory.write.MemoryInstanceIntWriterImpl
import io.github.charlietap.chasm.executor.memory.write.MemoryInstanceLongWriterImpl
import io.github.charlietap.chasm.executor.runtime.error.InvocationError
import io.github.charlietap.chasm.executor.runtime.ext.memory
import io.github.charlietap.chasm.executor.runtime.store.Address
import io.github.charlietap.chasm.executor.runtime.store.Store
import kotlinx.io.RawSink
import kotlinx.io.RawSource

internal class ChasmMemoryAdapter(
    private val store: Store,
    private val memoryAddress: Address.Memory,
) : Memory {
    val memoryInstance get() = store.memory(memoryAddress).getOrThrow { ChasmModuleRuntimeErrorException(it) }

    override fun readI8(addr: WasmPtr<*>): Byte {
        return readMemory(store, memoryAddress, addr.addr).orThrow()
    }

    override fun readI32(addr: WasmPtr<*>): Int {
        return MemoryInstanceIntReaderImpl(memoryInstance, addr.addr, 4).getOrThrow()
    }

    override fun readI64(addr: WasmPtr<*>): Long {
        return MemoryInstanceLongReaderImpl(memoryInstance, addr.addr, 8).getOrThrow()
    }

    override fun source(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSource {
        return ChasmMemoryRawSource(store, memoryAddress, fromAddr, toAddrExclusive)
    }

    override fun writeI8(addr: WasmPtr<*>, data: Byte) {
        writeMemory(store, memoryAddress, addr.addr, data).orThrow()
    }

    override fun writeI32(addr: WasmPtr<*>, data: Int) {
        return MemoryInstanceIntWriterImpl(memoryInstance, data, addr.addr, 4).getOrThrow()
    }

    override fun writeI64(addr: WasmPtr<*>, data: Long) {
        return MemoryInstanceLongWriterImpl(memoryInstance, data, addr.addr, 8).getOrThrow()
    }

    override fun sink(fromAddr: WasmPtr<*>, toAddrExclusive: WasmPtr<*>): RawSink {
        return ChasmMemoryRawSink(store, memoryAddress, fromAddr, toAddrExclusive)
    }

    fun grow(pagesToAdd: Int): Int {
        val oldPages = memoryInstance.data.min.amount
        return MemoryGrowerImpl(memoryInstance, pagesToAdd).fold(
            { newMemoryInstance ->
                store.memories[memoryAddress.address] = newMemoryInstance
                oldPages
            },
        ) {
            -1
        }
    }
}

private fun <T : Any, E : InvocationError> Result<T, E>.getOrThrow(): T = getOrThrow {
    ChasmModuleRuntimeErrorException(it)
}
