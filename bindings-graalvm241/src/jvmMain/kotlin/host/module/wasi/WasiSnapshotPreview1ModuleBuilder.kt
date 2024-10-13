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
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ArgsGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ArgsSizesGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdClose
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPrestatDirName
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPrestatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSeek
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdStatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSync
import at.released.weh.bindings.graalvm241.host.module.wasi.function.RandomGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SchedYield
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdPread
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdPwrite
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdRead
import at.released.weh.bindings.graalvm241.host.module.wasi.function.fdWrite
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import org.graalvm.polyglot.Context
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmModule

internal object WasiSnapshotPreview1ModuleBuilder {
    private val WasiPreview1HostFunction.nodeFactory: NodeFactory
        get() = when (this) {
            WasiPreview1HostFunction.ENVIRON_GET -> ::EnvironGet
            WasiPreview1HostFunction.ENVIRON_SIZES_GET -> ::EnvironSizesGet
            WasiPreview1HostFunction.FD_CLOSE -> ::FdClose
            WasiPreview1HostFunction.FD_PREAD -> ::fdPread
            WasiPreview1HostFunction.FD_PWRITE -> ::fdPwrite
            WasiPreview1HostFunction.FD_READ -> ::fdRead
            WasiPreview1HostFunction.FD_SEEK -> ::FdSeek
            WasiPreview1HostFunction.FD_SYNC -> ::FdSync
            WasiPreview1HostFunction.FD_WRITE -> ::fdWrite
            WasiPreview1HostFunction.SCHED_YIELD -> ::SchedYield
            WasiPreview1HostFunction.ARGS_GET -> ::ArgsGet
            WasiPreview1HostFunction.ARGS_SIZES_GET -> ::ArgsSizesGet
            WasiPreview1HostFunction.RANDOM_GET -> ::RandomGet
            WasiPreview1HostFunction.FD_FDSTAT_GET -> ::FdStatGet
            WasiPreview1HostFunction.FD_PRESTAT_DIR_NAME -> ::FdPrestatDirName
            WasiPreview1HostFunction.FD_PRESTAT_GET -> ::FdPrestatGet
            WasiPreview1HostFunction.CLOCK_RES_GET,
            WasiPreview1HostFunction.CLOCK_TIME_GET,
            WasiPreview1HostFunction.FD_ADVISE,
            WasiPreview1HostFunction.FD_ALLOCATE,
            WasiPreview1HostFunction.FD_DATASYNC,
            WasiPreview1HostFunction.FD_FDSTAT_SET_FLAGS,
            WasiPreview1HostFunction.FD_FDSTAT_SET_RIGHTS,
            WasiPreview1HostFunction.FD_FILESTAT_GET,
            WasiPreview1HostFunction.FD_FILESTAT_SET_SIZE,
            WasiPreview1HostFunction.FD_FILESTAT_SET_TIMES,
            WasiPreview1HostFunction.FD_READDIR,
            WasiPreview1HostFunction.FD_RENUMBER,
            WasiPreview1HostFunction.FD_TELL,
            WasiPreview1HostFunction.PATH_CREATE_DIRECTORY,
            WasiPreview1HostFunction.PATH_FILESTAT_GET,
            WasiPreview1HostFunction.PATH_FILESTAT_SET_TIMES,
            WasiPreview1HostFunction.PATH_LINK,
            WasiPreview1HostFunction.PATH_OPEN,
            WasiPreview1HostFunction.PATH_READLINK,
            WasiPreview1HostFunction.PATH_REMOVE_DIRECTORY,
            WasiPreview1HostFunction.PATH_RENAME,
            WasiPreview1HostFunction.PATH_SYMLINK,
            WasiPreview1HostFunction.PATH_UNLINK_FILE,
            WasiPreview1HostFunction.POLL_ONEOFF,
            WasiPreview1HostFunction.PROC_EXIT,
            WasiPreview1HostFunction.PROC_RAISE,
            WasiPreview1HostFunction.SOCK_ACCEPT,
            WasiPreview1HostFunction.SOCK_RECV,
            WasiPreview1HostFunction.SOCK_SEND,
            WasiPreview1HostFunction.SOCK_SHUTDOWN,
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
            functions = WasiPreview1HostFunction.entries.associateWith { it.nodeFactory },
        )
    }
}
