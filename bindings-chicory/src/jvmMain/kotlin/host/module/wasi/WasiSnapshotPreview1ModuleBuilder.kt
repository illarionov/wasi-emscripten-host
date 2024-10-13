/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NoMultipleSpaces")

package at.released.weh.bindings.chicory.host.module.wasi

import at.released.weh.bindings.chicory.ext.wasmValueTypeToChicoryValueType
import at.released.weh.bindings.chicory.host.module.wasi.function.ArgsGet
import at.released.weh.bindings.chicory.host.module.wasi.function.ArgsSizesGet
import at.released.weh.bindings.chicory.host.module.wasi.function.EnvironGet
import at.released.weh.bindings.chicory.host.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.chicory.host.module.wasi.function.FdClose
import at.released.weh.bindings.chicory.host.module.wasi.function.FdPrestatDirName
import at.released.weh.bindings.chicory.host.module.wasi.function.FdPrestatGet
import at.released.weh.bindings.chicory.host.module.wasi.function.FdReadFdPread.Companion.fdPread
import at.released.weh.bindings.chicory.host.module.wasi.function.FdReadFdPread.Companion.fdRead
import at.released.weh.bindings.chicory.host.module.wasi.function.FdSeek
import at.released.weh.bindings.chicory.host.module.wasi.function.FdSync
import at.released.weh.bindings.chicory.host.module.wasi.function.FdWriteFdPwrite.Companion.fdPwrite
import at.released.weh.bindings.chicory.host.module.wasi.function.FdWriteFdPwrite.Companion.fdWrite
import at.released.weh.bindings.chicory.host.module.wasi.function.FdstatGet
import at.released.weh.bindings.chicory.host.module.wasi.function.NotImplemented
import at.released.weh.bindings.chicory.host.module.wasi.function.RandomGet
import at.released.weh.bindings.chicory.host.module.wasi.function.SchedYield
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ARGS_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ARGS_SIZES_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ENVIRON_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ENVIRON_SIZES_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_CLOSE
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_FDSTAT_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PREAD
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PRESTAT_DIR_NAME
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PRESTAT_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PWRITE
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_READ
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_SEEK
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_SYNC
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_WRITE
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.RANDOM_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.SCHED_YIELD
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
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
        return WasiPreview1HostFunction.entries.map { wasiFunc ->
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

    @Suppress("CyclomaticComplexMethod")
    private fun WasiPreview1HostFunction.createWasiHostFunctionHandle(
        host: EmbedderHost,
        memory: Memory,
        wasiMemoryReader: WasiMemoryReader,
        wasiMemoryWriter: WasiMemoryWriter,
    ): WasiHostFunctionHandle = when (this) {
        ARGS_GET -> ArgsGet(host, memory)
        ARGS_SIZES_GET -> ArgsSizesGet(host, memory)
        ENVIRON_GET -> EnvironGet(host, memory)
        ENVIRON_SIZES_GET -> EnvironSizesGet(host, memory)
        FD_CLOSE -> FdClose(host)
        FD_FDSTAT_GET -> FdstatGet(host, memory)
        FD_PREAD -> fdPread(host, memory, wasiMemoryReader)
        FD_PRESTAT_GET -> FdPrestatGet(host, memory)
        FD_PRESTAT_DIR_NAME -> FdPrestatDirName(host, memory)
        FD_PWRITE -> fdPwrite(host, memory, wasiMemoryWriter)
        FD_READ -> fdRead(host, memory, wasiMemoryReader)
        FD_SEEK -> FdSeek(host, memory)
        FD_SYNC -> FdSync(host)
        FD_WRITE -> fdWrite(host, memory, wasiMemoryWriter)
        SCHED_YIELD -> SchedYield(host)
        RANDOM_GET -> RandomGet(host, memory)
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
