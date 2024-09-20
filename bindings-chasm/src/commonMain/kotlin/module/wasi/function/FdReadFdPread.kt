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
import at.released.weh.wasi.preview1.function.FdReadFdPreadFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.shapes.Value

internal class FdReadFdPread private constructor(
    private val memory: Memory,
    private val memoryReader: WasiMemoryReader,
    private val handle: FdReadFdPreadFunctionHandle,
) : WasiHostFunctionHandle {
    override operator fun invoke(args: List<Value>): Errno {
        val fd = args[0].asInt()

        @IntWasmPtr(Iovec::class)
        val pIov: WasmPtr = args[1].asWasmAddr()

        val iovCnt = args[2].asInt()

        @IntWasmPtr(Int::class)
        val pNum: WasmPtr = args[3].asWasmAddr()

        return handle.execute(memory, memoryReader, fd, pIov, iovCnt, pNum)
    }

    companion object {
        fun fdRead(
            host: EmbedderHost,
            memory: Memory,
            memoryReader: WasiMemoryReader,
        ): WasiHostFunctionHandle = FdReadFdPread(memory, memoryReader, FdReadFdPreadFunctionHandle.fdRead(host))

        fun fdPread(
            host: EmbedderHost,
            memory: Memory,
            memoryReader: WasiMemoryReader,
        ): WasiHostFunctionHandle = FdReadFdPread(memory, memoryReader, FdReadFdPreadFunctionHandle.fdPread(host))
    }
}
