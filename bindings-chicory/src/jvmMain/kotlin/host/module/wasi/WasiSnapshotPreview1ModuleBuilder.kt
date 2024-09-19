/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NoMultipleSpaces")

package at.released.weh.bindings.chicory.host.module.wasi

import at.released.weh.bindings.chicory.ext.wasmValueTypeToChicoryValueType
import at.released.weh.bindings.chicory.host.module.wasi.function.EnvironGet
import at.released.weh.bindings.chicory.host.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.chicory.host.module.wasi.function.FdClose
import at.released.weh.bindings.chicory.host.module.wasi.function.FdReadFdPread.Companion.fdPread
import at.released.weh.bindings.chicory.host.module.wasi.function.FdReadFdPread.Companion.fdRead
import at.released.weh.bindings.chicory.host.module.wasi.function.FdSeek
import at.released.weh.bindings.chicory.host.module.wasi.function.FdSync
import at.released.weh.bindings.chicory.host.module.wasi.function.FdWriteFdPwrite.Companion.fdPwrite
import at.released.weh.bindings.chicory.host.module.wasi.function.FdWriteFdPwrite.Companion.fdWrite
import at.released.weh.bindings.chicory.host.module.wasi.function.NotImplemented
import at.released.weh.bindings.chicory.host.module.wasi.function.SchedYield
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasi.preview1.WasiHostFunction.ENVIRON_GET
import at.released.weh.wasi.preview1.WasiHostFunction.ENVIRON_SIZES_GET
import at.released.weh.wasi.preview1.WasiHostFunction.FD_CLOSE
import at.released.weh.wasi.preview1.WasiHostFunction.FD_PREAD
import at.released.weh.wasi.preview1.WasiHostFunction.FD_PWRITE
import at.released.weh.wasi.preview1.WasiHostFunction.FD_READ
import at.released.weh.wasi.preview1.WasiHostFunction.FD_SEEK
import at.released.weh.wasi.preview1.WasiHostFunction.FD_SYNC
import at.released.weh.wasi.preview1.WasiHostFunction.FD_WRITE
import at.released.weh.wasi.preview1.WasiHostFunction.SCHED_YIELD
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasm.core.memory.Memory
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value
import com.dylibso.chicory.runtime.WasmFunctionHandle as ChicoryWasmFunctionHandle

// https://github.com/WebAssembly/WASI/tree/main
internal class WasiSnapshotPreview1ModuleBuilder(
    private val memory: Memory,
    private val wasiMemoryReader: WasiMemoryReader,
    private val wasiMemoryWriter: WasiMemoryWriter,
    private val host: EmbedderHost,
) {
    fun asChicoryHostFunctions(
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ): List<HostFunction> {
        return WasiHostFunction.entries.map { wasiFunc ->
            val functionHandle = wasiFunc.createWasiHostFunctionHandle(host, memory, wasiMemoryReader, wasiMemoryWriter)
            HostFunction(
                WasiHostFunctionAdapter(functionHandle),
                moduleName,
                wasiFunc.wasmName,
                wasiFunc.type.paramTypes.map(::wasmValueTypeToChicoryValueType),
                wasiFunc.type.returnTypes.map(::wasmValueTypeToChicoryValueType),
            )
        }
    }

    private fun WasiHostFunction.createWasiHostFunctionHandle(
        host: EmbedderHost,
        memory: Memory,
        wasiMemoryReader: WasiMemoryReader,
        wasiMemoryWriter: WasiMemoryWriter,
    ): WasiHostFunctionHandle = when (this) {
        ENVIRON_GET -> EnvironGet(host, memory)
        ENVIRON_SIZES_GET -> EnvironSizesGet(host, memory)
        FD_CLOSE -> FdClose(host)
        FD_PREAD -> fdPread(host, memory, wasiMemoryReader)
        FD_PWRITE -> fdPwrite(host, memory, wasiMemoryWriter)
        FD_READ -> fdRead(host, memory, wasiMemoryReader)
        FD_SEEK -> FdSeek(host, memory)
        FD_SYNC -> FdSync(host)
        FD_WRITE -> fdWrite(host, memory, wasiMemoryWriter)
        SCHED_YIELD -> SchedYield(host)
        else -> NotImplemented
    }

    private class WasiHostFunctionAdapter(
        private val delegate: WasiHostFunctionHandle,
    ) : ChicoryWasmFunctionHandle {
        override fun apply(instance: Instance, vararg args: Value): Array<Value> {
            val result = delegate.apply(instance, args = args)
            return arrayOf(Value.i32(result.code.toLong()))
        }
    }
}
