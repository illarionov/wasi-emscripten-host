/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.wasi

import at.released.weh.bindings.graalvm241.MemorySource
import at.released.weh.bindings.graalvm241.ext.WasmModuleMemoryHelper
import at.released.weh.bindings.graalvm241.ext.setupWasmModuleFunctions
import at.released.weh.bindings.graalvm241.ext.withWasmContext
import at.released.weh.bindings.graalvm241.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.host.module.NodeFactory
import at.released.weh.bindings.graalvm241.host.module.notImplementedFunctionNodeFactory
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdClose
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSeek
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSync
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SchedYield
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdPread
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdPwrite
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdRead
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdWrite
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import at.released.weh.host.wasi.preview1.WasiHostFunction
import org.graalvm.polyglot.Context
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmModule

internal object WasiSnapshotPreview1ModuleBuilder {
    private val WasiHostFunction.nodeFactory: NodeFactory
        get() = when (this) {
            WasiHostFunction.ENVIRON_GET -> ::EnvironGet
            WasiHostFunction.ENVIRON_SIZES_GET -> ::EnvironSizesGet
            WasiHostFunction.FD_CLOSE -> ::FdClose
            WasiHostFunction.FD_PREAD -> ::fdPread
            WasiHostFunction.FD_PWRITE -> ::fdPwrite
            WasiHostFunction.FD_READ -> ::fdRead
            WasiHostFunction.FD_SEEK -> ::FdSeek
            WasiHostFunction.FD_SYNC -> ::FdSync
            WasiHostFunction.FD_WRITE -> ::fdWrite
            WasiHostFunction.SCHED_YIELD -> ::SchedYield
            WasiHostFunction.ARGS_GET,
            WasiHostFunction.ARGS_SIZES_GET,
            WasiHostFunction.CLOCK_RES_GET,
            WasiHostFunction.CLOCK_TIME_GET,
            WasiHostFunction.FD_ADVISE,
            WasiHostFunction.FD_ALLOCATE,
            WasiHostFunction.FD_DATASYNC,
            WasiHostFunction.FD_FDSTAT_GET,
            WasiHostFunction.FD_FDSTAT_SET_FLAGS,
            WasiHostFunction.FD_FDSTAT_SET_RIGHTS,
            WasiHostFunction.FD_FILESTAT_GET,
            WasiHostFunction.FD_FILESTAT_SET_SIZE,
            WasiHostFunction.FD_FILESTAT_SET_TIMES,
            WasiHostFunction.FD_PRESTAT_DIR_NAME,
            WasiHostFunction.FD_PRESTAT_GET,
            WasiHostFunction.FD_READDIR,
            WasiHostFunction.FD_RENUMBER,
            WasiHostFunction.FD_TELL,
            WasiHostFunction.PATH_CREATE_DIRECTORY,
            WasiHostFunction.PATH_FILESTAT_GET,
            WasiHostFunction.PATH_FILESTAT_SET_TIMES,
            WasiHostFunction.PATH_LINK,
            WasiHostFunction.PATH_OPEN,
            WasiHostFunction.PATH_READLINK,
            WasiHostFunction.PATH_REMOVE_DIRECTORY,
            WasiHostFunction.PATH_RENAME,
            WasiHostFunction.PATH_SYMLINK,
            WasiHostFunction.PATH_UNLINK_FILE,
            WasiHostFunction.POLL_ONEOFF,
            WasiHostFunction.PROC_EXIT,
            WasiHostFunction.PROC_RAISE,
            WasiHostFunction.RANDOM_GET,
            WasiHostFunction.SOCK_ACCEPT,
            WasiHostFunction.SOCK_RECV,
            WasiHostFunction.SOCK_SEND,
            WasiHostFunction.SOCK_SHUTDOWN,
                -> notImplementedFunctionNodeFactory(this)
        }

    fun setupModule(
        graalContext: Context,
        host: EmbedderHost,
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
        memory: MemorySource?,
        memoryWaiters: SharedMemoryWaiterListStore,
    ): WasmInstance = graalContext.withWasmContext { wasmContext ->
        val wasiModule = WasmModule.create(moduleName, null)
        memory?.let {
            WasmModuleMemoryHelper(wasiModule).setupMemory(
                source = it,
                memoryWaiters = memoryWaiters,
                logger = host.rootLogger,
            )
        }

        return setupWasmModuleFunctions(
            context = wasmContext,
            host = host,
            module = wasiModule,
            functions = WasiHostFunction.entries.associateWith { it.nodeFactory },
        )
    }
}
