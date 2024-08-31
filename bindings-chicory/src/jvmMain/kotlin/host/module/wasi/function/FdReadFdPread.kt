/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.wasi.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.wasi.WasiHostFunctionHandle
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.WasiMemoryReader
import at.released.weh.host.wasi.preview1.function.FdReadFdPreadFunctionHandle
import at.released.weh.host.wasi.preview1.type.Iovec
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class FdReadFdPread private constructor(
    private val memory: Memory,
    private val wasiMemoryReader: WasiMemoryReader,
    private val handle: FdReadFdPreadFunctionHandle,
) : WasiHostFunctionHandle {
    override fun apply(instance: Instance, vararg args: Value): Errno {
        val fd = Fd(args[0].asInt())
        val pIov: WasmPtr<Iovec> = args[1].asWasmAddr()
        val iovCnt = args[2].asInt()
        val pNum: WasmPtr<Int> = args[3].asWasmAddr()
        return handle.execute(memory, wasiMemoryReader, fd, pIov, iovCnt, pNum)
    }

    companion object {
        fun fdRead(
            host: EmbedderHost,
            memory: Memory,
            wasiMemoryReader: WasiMemoryReader,
        ): WasiHostFunctionHandle = FdReadFdPread(memory, wasiMemoryReader, FdReadFdPreadFunctionHandle.fdRead(host))

        fun fdPread(
            host: EmbedderHost,
            memory: Memory,
            wasiMemoryReader: WasiMemoryReader,
        ): WasiHostFunctionHandle = FdReadFdPread(memory, wasiMemoryReader, FdReadFdPreadFunctionHandle.fdPread(host))
    }
}
