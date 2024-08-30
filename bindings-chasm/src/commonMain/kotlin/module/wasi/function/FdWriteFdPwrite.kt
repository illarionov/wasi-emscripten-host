/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.WasiMemoryWriter
import at.released.weh.host.wasi.preview1.function.FdWriteFdPWriteFunctionHandle
import at.released.weh.host.wasi.preview1.type.CioVec
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue

internal class FdWriteFdPwrite private constructor(
    private val memory: Memory,
    private val wasiMemoryWriter: WasiMemoryWriter,
    private val handle: FdWriteFdPWriteFunctionHandle,
) : WasiHostFunctionHandle {
    override fun invoke(args: List<ExecutionValue>): Errno {
        val fd = Fd(args[0].asInt())
        val pCiov: WasmPtr<CioVec> = args[1].asWasmAddr()
        val cIovCnt = args[2].asInt()
        val pNum: WasmPtr<Int> = args[3].asWasmAddr()
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
