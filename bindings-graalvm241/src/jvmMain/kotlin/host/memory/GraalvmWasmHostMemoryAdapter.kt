/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.memory

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import com.oracle.truffle.api.nodes.Node
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import org.graalvm.wasm.memory.WasmMemory

/**
 * [Memory] implementation based on GraalVM [WasmMemory]
 */
@InternalWasiEmscriptenHostApi
public class GraalvmWasmHostMemoryAdapter(
    private val memoryProvider: () -> WasmMemory,
    internal val node: Node?,
) : Memory {
    internal val wasmMemory: WasmMemory get() = memoryProvider.invoke()

    override fun readI8(@IntWasmPtr addr: WasmPtr): Byte {
        return wasmMemory.load_i32_8u(node, addr.toLong()).toByte()
    }

    override fun readI32(@IntWasmPtr addr: WasmPtr): Int {
        return wasmMemory.load_i32(node, addr.toLong())
    }

    override fun readI64(@IntWasmPtr addr: WasmPtr): Long {
        return wasmMemory.load_i64(node, addr.toLong())
    }

    override fun source(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSource {
        return GraalvmMemoryRawSource(memoryProvider, fromAddr, toAddrExclusive, node)
    }

    override fun writeI8(@IntWasmPtr addr: WasmPtr, data: Byte) {
        wasmMemory.store_i32_8(node, addr.toLong(), data)
    }

    override fun writeI32(@IntWasmPtr addr: WasmPtr, data: Int) {
        wasmMemory.store_i32(node, addr.toLong(), data)
    }

    override fun writeI64(@IntWasmPtr addr: WasmPtr, data: Long) {
        wasmMemory.store_i64(node, addr.toLong(), data)
    }

    override fun sink(@IntWasmPtr fromAddr: WasmPtr, @IntWasmPtr toAddrExclusive: WasmPtr): RawSink {
        return GraalvmMemoryRawSink(memoryProvider, fromAddr, toAddrExclusive)
    }
}
