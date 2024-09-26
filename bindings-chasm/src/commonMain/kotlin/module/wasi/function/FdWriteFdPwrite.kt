/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdWriteFdPWriteFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Ciovec
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.shapes.Value

internal class FdWriteFdPwrite private constructor(
    private val memory: Memory,
    private val wasiMemoryWriter: WasiMemoryWriter,
    private val handle: FdWriteFdPWriteFunctionHandle,
) : WasiHostFunctionHandle {
    override operator fun invoke(args: List<Value>): Errno {
        val fd = args[0].asInt()

        @IntWasmPtr(Ciovec::class)
        val pCiov: WasmPtr = args[1].asWasmAddr()

        val cIovCnt = args[2].asInt()

        @IntWasmPtr(Int::class)
        val pNum: WasmPtr = args[3].asWasmAddr()

        return handle.execute(memory, wasiMemoryWriter, fd, pCiov, cIovCnt, pNum)
    }

    companion object {
        fun fdWrite(
            host: EmbedderHost,
            memory: Memory,
            wasiMemoryWriter: WasiMemoryWriter,
        ): WasiHostFunctionHandle = FdWriteFdPwrite(
            memory,
            wasiMemoryWriter,
            FdWriteFdPWriteFunctionHandle.fdWrite(host),
        )

        fun fdPwrite(
            host: EmbedderHost,
            memory: Memory,
            wasiMemoryWriter: WasiMemoryWriter,
        ): WasiHostFunctionHandle = FdWriteFdPwrite(
            memory,
            wasiMemoryWriter,
            FdWriteFdPWriteFunctionHandle.fdPwrite(host),
        )
    }
}
