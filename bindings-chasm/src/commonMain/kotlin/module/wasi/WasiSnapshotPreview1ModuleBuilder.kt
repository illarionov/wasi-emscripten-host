/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi

import at.released.weh.bindings.chasm.ext.toChasmFunctionTypes
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
import at.released.weh.bindings.chasm.module.wasi.function.EnvironGet
import at.released.weh.bindings.chasm.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.chasm.module.wasi.function.FdClose
import at.released.weh.bindings.chasm.module.wasi.function.FdReadFdPread.Companion.fdPread
import at.released.weh.bindings.chasm.module.wasi.function.FdReadFdPread.Companion.fdRead
import at.released.weh.bindings.chasm.module.wasi.function.FdSeek
import at.released.weh.bindings.chasm.module.wasi.function.FdSync
import at.released.weh.bindings.chasm.module.wasi.function.FdWriteFdPwrite.Companion.fdPwrite
import at.released.weh.bindings.chasm.module.wasi.function.FdWriteFdPwrite.Companion.fdWrite
import at.released.weh.bindings.chasm.module.wasi.function.NotImplementedWasiFunction
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ENVIRON_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.ENVIRON_SIZES_GET
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_CLOSE
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PREAD
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_PWRITE
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_READ
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_SEEK
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_SYNC
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_WRITE
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.function
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.HostFunction as ChasmHostFunction
import io.github.charlietap.chasm.embedding.shapes.Import as ChasmImport

internal fun createWasiPreview1HostFunctions(
    store: Store,
    memory: ChasmMemoryAdapter,
    wasiMemoryReader: WasiMemoryReader,
    wasiMemoryWriter: WasiMemoryWriter,
    host: EmbedderHost,
    moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
): List<ChasmImport> {
    val functionTypes = WasiPreview1HostFunction.entries.map(WasiPreview1HostFunction::type).toChasmFunctionTypes()
    return WasiPreview1HostFunction.entries.map { wasiFunc ->
        ChasmImport(
            moduleName = moduleName,
            entityName = wasiFunc.wasmName,
            value = function(
                store = store,
                type = functionTypes.getValue(wasiFunc.type),
                function = wasiFunc.createWasiHostFunctionHandle(host, memory, wasiMemoryReader, wasiMemoryWriter)
                    .toChasmHostFunction(),
            ),
        )
    }
}

private fun WasiHostFunctionHandle.toChasmHostFunction(): ChasmHostFunction = { args ->
    listOf(Value.Number.I32(this@toChasmHostFunction(args).code))
}

private fun WasiPreview1HostFunction.createWasiHostFunctionHandle(
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
    else -> NotImplementedWasiFunction(this)
}
