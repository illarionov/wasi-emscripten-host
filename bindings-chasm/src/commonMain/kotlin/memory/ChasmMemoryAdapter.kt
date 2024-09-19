/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.bindings.chasm.memory

import at.released.weh.bindings.chasm.ext.orThrow
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import com.github.michaelbull.result.fold
import io.github.charlietap.chasm.embedding.memory.readByte
import io.github.charlietap.chasm.embedding.memory.readBytes
import io.github.charlietap.chasm.embedding.memory.writeByte
import io.github.charlietap.chasm.embedding.memory.writeBytes
import io.github.charlietap.chasm.embedding.shapes.Limits
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.executor.memory.grow.MemoryGrowerImpl
import io.github.charlietap.chasm.executor.runtime.instance.ExternalValue
import io.github.charlietap.chasm.executor.runtime.instance.MemoryInstance
import io.github.charlietap.chasm.executor.runtime.store.Address
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import io.github.charlietap.chasm.embedding.shapes.Memory as ChasmMemory

public class ChasmMemoryAdapter(
    private val store: Store,
    memoryProvider: (Store.() -> ChasmMemory)?,
) : Memory {
    private val memoryProvider: (Store) -> ChasmMemory = memoryProvider ?: Store::defaultMemory

    public val memoryInstance: ChasmMemory
        get() = memoryProvider(store)

    // XXX: rewrite without using internal APIs
    @Suppress("INVISIBLE_MEMBER")
    public val limits: Limits
        get() {
            val rawMemoryAddress = memoryInstance.reference.address
            val rawInstanceData = store.store.memories[rawMemoryAddress.address].data
            return Limits(
                min = rawInstanceData.min.amount.toUInt(),
                max = rawInstanceData.max?.amount?.toUInt(),
            )
        }

    override fun readI8(@IntWasmPtr addr: WasmPtr): Byte {
        return readByte(store, memoryInstance, addr).orThrow()
    }

    override fun readI32(@IntWasmPtr addr: WasmPtr): Int {
        val buffer = ByteArray(4)
        val bytes = readBytes(store, memoryInstance, buffer, addr, 4).orThrow()
        return (bytes[0].toInt() and 0xff) or
                ((bytes[1].toInt() and 0xff) shl 8) or
                ((bytes[2].toInt() and 0xff) shl 16) or
                ((bytes[3].toInt() and 0xff) shl 24)
    }

    override fun readI64(@IntWasmPtr addr: WasmPtr): Long {
        val buffer = ByteArray(8)
        val bytes = readBytes(store, memoryInstance, buffer, addr, 8).orThrow()
        return (bytes[0].toLong() and 0xffL) or
                (bytes[1].toLong() and 0xffL shl 8) or
                (bytes[2].toLong() and 0xffL shl 16) or
                (bytes[3].toLong() and 0xffL shl 24) or
                (bytes[4].toLong() and 0xffL shl 32) or
                (bytes[5].toLong() and 0xffL shl 40) or
                (bytes[6].toLong() and 0xffL shl 48) or
                (bytes[7].toLong() and 0xffL shl 56)
    }

    override fun source(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSource {
        return ChasmMemoryRawSource(store, memoryInstance, fromAddr, toAddrExclusive)
    }

    override fun writeI8(@IntWasmPtr addr: WasmPtr, data: Byte) {
        writeByte(store, memoryInstance, addr, data).orThrow()
    }

    override fun writeI32(@IntWasmPtr addr: WasmPtr, data: Int) {
        val bytes = ByteArray(4) {
            (data ushr 8 * it and 0xff).toByte()
        }
        writeBytes(store, memoryInstance, addr, bytes).orThrow()
    }

    override fun writeI64(addr: WasmPtr, data: Long) {
        val bytes = ByteArray(8) {
            (data ushr 8 * it and 0xff).toByte()
        }
        writeBytes(store, memoryInstance, addr, bytes).orThrow()
    }

    override fun sink(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSink {
        return ChasmMemoryRawSink(store, memoryInstance, fromAddr, toAddrExclusive)
    }

    // XX: should be removed
    @Suppress("INVISIBLE_MEMBER")
    public fun grow(pagesToAdd: Int): Int {
        val rawMemoryAddress = memoryInstance.reference.address
        val rawInstance: MemoryInstance = store.store.memories[rawMemoryAddress.address]
        val oldPages = rawInstance.data.min.amount
        return MemoryGrowerImpl(rawInstance, pagesToAdd).fold(
            { newMemoryInstance ->
                store.store.memories[rawMemoryAddress.address] = newMemoryInstance
                oldPages
            },
        ) {
            -1
        }
    }
}

private fun Store.defaultMemory(): ChasmMemory {
    @Suppress("INVISIBLE_MEMBER")
    return ChasmMemory(ExternalValue.Memory(Address.Memory(0)))
}
