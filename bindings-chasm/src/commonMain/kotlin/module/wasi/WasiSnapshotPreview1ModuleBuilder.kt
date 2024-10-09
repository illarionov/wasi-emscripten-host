/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi

import at.released.weh.bindings.chasm.exception.ProcExitException
import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.ext.toChasmFunctionTypes
import at.released.weh.bindings.chasm.memory.ChasmMemoryAdapter
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
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.PROC_EXIT
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.RANDOM_GET
import at.released.weh.wasi.preview1.function.ArgsGetFunctionHandle
import at.released.weh.wasi.preview1.function.ArgsSizesGetHostFunctionHandle
import at.released.weh.wasi.preview1.function.EnvironGetFunctionHandle
import at.released.weh.wasi.preview1.function.EnvironSizesGetFunctionHandle
import at.released.weh.wasi.preview1.function.FdCloseFunctionHandle
import at.released.weh.wasi.preview1.function.FdPrestatDirNameFunctionHandle
import at.released.weh.wasi.preview1.function.FdPrestatGetFunctionHandle
import at.released.weh.wasi.preview1.function.FdReadFdPreadFunctionHandle
import at.released.weh.wasi.preview1.function.FdSeekFunctionHandle
import at.released.weh.wasi.preview1.function.FdSyncFunctionHandle
import at.released.weh.wasi.preview1.function.FdWriteFdPWriteFunctionHandle
import at.released.weh.wasi.preview1.function.FdstatGetFunctionHandle
import at.released.weh.wasi.preview1.function.RandomGetFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.HostFunction
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.function
import io.github.charlietap.chasm.embedding.shapes.HostFunctionContext
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.Value.Number.I32
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
    val functions = ChasmWasiPreview1Functions(host, memory, wasiMemoryReader, wasiMemoryWriter)
    return WasiPreview1HostFunction.entries.map { wasiFunc: WasiPreview1HostFunction ->
        ChasmImport(
            moduleName = moduleName,
            entityName = wasiFunc.wasmName,
            value = function(
                store = store,
                type = functionTypes.getValue(wasiFunc.type),
                function = createChasmHostFunction(wasiFunc, functions),
            ),
        )
    }
}

@Suppress("CyclomaticComplexMethod")
private fun createChasmHostFunction(
    wasiHostFunction: WasiPreview1HostFunction,
    functions: ChasmWasiPreview1Functions,
): ChasmHostFunction = when (wasiHostFunction) {
    ARGS_GET -> functions.argsGet
    ARGS_SIZES_GET -> functions.argsSizesGet
    ENVIRON_GET -> functions.environGet
    ENVIRON_SIZES_GET -> functions.environSizesGet
    FD_CLOSE -> functions.fdClose
    FD_FDSTAT_GET -> functions.fdstatGet
    FD_PREAD -> functions.fdPread
    FD_PRESTAT_DIR_NAME -> functions.fdPrestatDirname
    FD_PRESTAT_GET -> functions.fdPrestatGet
    FD_PWRITE -> functions.fdPwrite
    FD_READ -> functions.fdRead
    FD_SEEK -> functions.fdSeek
    FD_SYNC -> functions.fdSync
    FD_WRITE -> functions.fdwrite
    PROC_EXIT -> functions.procExit
    RANDOM_GET -> functions.randomGet
    else -> NotImplementedWasiFunction(wasiHostFunction)::invoke
}

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
private class ChasmWasiPreview1Functions(
    host: EmbedderHost,
    private val memory: Memory,
    private val wasiMemoryReader: WasiMemoryReader,
    private val wasiMemoryWriter: WasiMemoryWriter,
) {
    private val argsGetHandle = ArgsGetFunctionHandle(host)
    private val argsSizesGetHandle = ArgsSizesGetHostFunctionHandle(host)
    private val environGetHandle = EnvironGetFunctionHandle(host)
    private val environSizesGetHandle = EnvironSizesGetFunctionHandle(host)
    private val fdCloseHandle = FdCloseFunctionHandle(host)
    private val fdReadHandle = FdReadFdPreadFunctionHandle.fdRead(host)
    private val fdstatGetHandle = FdstatGetFunctionHandle(host)
    private val fdPreadHandle = FdReadFdPreadFunctionHandle.fdPread(host)
    private val fdPrestatGetHandle = FdPrestatGetFunctionHandle(host)
    private val fdPrestatDirNameHandle = FdPrestatDirNameFunctionHandle(host)
    private val fdWriteHandle = FdWriteFdPWriteFunctionHandle.fdWrite(host)
    private val fdPwriteHandle = FdWriteFdPWriteFunctionHandle.fdPwrite(host)
    private val fdSeekHandle = FdSeekFunctionHandle(host)
    private val fdSyncHandle = FdSyncFunctionHandle(host)
    private val randomGetHandle = RandomGetFunctionHandle(host)

    val argsGet: ChasmHostFunction = { args ->
        argsGetHandle.execute(memory, args[0].asWasmAddr(), args[1].asWasmAddr()).toListOfReturnValues()
    }

    val argsSizesGet: ChasmHostFunction = { args ->
        argsSizesGetHandle.execute(memory, args[0].asWasmAddr(), args[1].asWasmAddr()).toListOfReturnValues()
    }

    val environGet: ChasmHostFunction = { args ->
        environGetHandle.execute(memory, args[0].asWasmAddr(), args[1].asWasmAddr()).toListOfReturnValues()
    }

    val environSizesGet: ChasmHostFunction = { args ->
        environSizesGetHandle.execute(memory, args[0].asWasmAddr(), args[1].asWasmAddr()).toListOfReturnValues()
    }

    val fdClose: ChasmHostFunction = { args ->
        fdCloseHandle.execute(args[0].asInt()).toListOfReturnValues()
    }

    val fdstatGet: ChasmHostFunction = { args ->
        fdstatGetHandle.execute(memory, args[0].asInt(), args[1].asWasmAddr()).toListOfReturnValues()
    }

    val fdRead: ChasmHostFunction = { args ->
        fdReadHandle.execute(
            memory = memory,
            bulkReader = wasiMemoryReader,
            fd = args[0].asInt(),
            pIov = args[1].asWasmAddr(),
            iovCnt = args[2].asInt(),
            pNum = args[3].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdPread: ChasmHostFunction = { args ->
        fdPreadHandle.execute(
            memory = memory,
            bulkReader = wasiMemoryReader,
            fd = args[0].asInt(),
            pIov = args[1].asWasmAddr(),
            iovCnt = args[2].asInt(),
            pNum = args[3].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdPrestatGet: ChasmHostFunction = { args ->
        fdPrestatGetHandle.execute(
            memory = memory,
            fd = args[0].asInt(),
            dstAddr = args[1].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdPrestatDirname: ChasmHostFunction = { args ->
        fdPrestatDirNameHandle.execute(
            memory = memory,
            fd = args[0].asInt(),
            dstPath = args[1].asWasmAddr(),
            dstPathLen = args[2].asInt(),
        ).toListOfReturnValues()
    }

    val fdPwrite: ChasmHostFunction = { args ->
        fdPwriteHandle.execute(
            memory = memory,
            bulkWriter = wasiMemoryWriter,
            fd = args[0].asInt(),
            pCiov = args[1].asWasmAddr(),
            cIovCnt = args[2].asInt(),
            pNum = args[3].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdwrite: ChasmHostFunction = { args ->
        fdWriteHandle.execute(
            memory = memory,
            bulkWriter = wasiMemoryWriter,
            fd = args[0].asInt(),
            pCiov = args[1].asWasmAddr(),
            cIovCnt = args[2].asInt(),
            pNum = args[3].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdSeek: ChasmHostFunction = { args ->
        fdSeekHandle.execute(
            memory = memory,
            fd = args[0].asInt(),
            offset = args[1].asLong(),
            whenceInt = args[2].asInt(),
            pNewOffset = args[3].asWasmAddr(),
        ).toListOfReturnValues()
    }

    val fdSync: ChasmHostFunction = { args ->
        fdSyncHandle.execute(
            fd = args[0].asInt(),
        ).toListOfReturnValues()
    }

    val randomGet: ChasmHostFunction = { args ->
        randomGetHandle.execute(
            memory = memory,
            buf = args[0].asWasmAddr(),
            bufLen = args[1].asInt(),
        ).toListOfReturnValues()
    }

    val procExit: ChasmHostFunction = { args ->
        val exitCode = args[0].asInt()
        // XXX: Need a way to stop a virtual machine from within a function. Using exception For now.
        throw ProcExitException(exitCode)
    }

    private fun Errno.toListOfReturnValues(): List<Value> = listOf(I32(this.code))
}

private class NotImplementedWasiFunction(
    private val function: HostFunction,
) {
    @Suppress("UnusedParameter")
    fun invoke(context: HostFunctionContext, args: List<Value>): Nothing {
        error("Function `$function` not implemented")
    }
}
