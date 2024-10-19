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
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ArgsGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ArgsSizesGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ClockResGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ClockTimeGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.EnvironSizesGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdAdvise
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdAllocate
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdClose
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdDatasync
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFdstatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFdstatSetFlags
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFdstatSetRights
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFilestatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFilestatSetSize
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdFilestatSetTimes
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPread
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPrestatDirName
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPrestatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdPwrite
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdRead
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdReaddir
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdRenumber
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSeek
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdSync
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdTell
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdWrite
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathCreateDirectory
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathFilestatGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathFilestatSetTimes
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathLink
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathOpen
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathReadlink
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathRemoveDirectory
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathRename
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathSymlink
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PathUnlinkFile
import at.released.weh.bindings.graalvm241.host.module.wasi.function.PollOneoff
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ProcExit
import at.released.weh.bindings.graalvm241.host.module.wasi.function.ProcRaise
import at.released.weh.bindings.graalvm241.host.module.wasi.function.RandomGet
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SchedYield
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SockAccept
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SockRecv
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SockSend
import at.released.weh.bindings.graalvm241.host.module.wasi.function.SockShutdown
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
            WasiPreview1HostFunction.FD_PREAD -> ::FdPread
            WasiPreview1HostFunction.FD_PWRITE -> ::FdPwrite
            WasiPreview1HostFunction.FD_READ -> ::FdRead
            WasiPreview1HostFunction.FD_SEEK -> ::FdSeek
            WasiPreview1HostFunction.FD_SYNC -> ::FdSync
            WasiPreview1HostFunction.FD_WRITE -> ::FdWrite
            WasiPreview1HostFunction.SCHED_YIELD -> ::SchedYield
            WasiPreview1HostFunction.ARGS_GET -> ::ArgsGet
            WasiPreview1HostFunction.ARGS_SIZES_GET -> ::ArgsSizesGet
            WasiPreview1HostFunction.RANDOM_GET -> ::RandomGet
            WasiPreview1HostFunction.FD_FDSTAT_GET -> ::FdFdstatGet
            WasiPreview1HostFunction.FD_PRESTAT_DIR_NAME -> ::FdPrestatDirName
            WasiPreview1HostFunction.FD_PRESTAT_GET -> ::FdPrestatGet
            WasiPreview1HostFunction.PATH_OPEN -> ::PathOpen
            WasiPreview1HostFunction.CLOCK_RES_GET -> ::ClockResGet
            WasiPreview1HostFunction.CLOCK_TIME_GET -> ::ClockTimeGet
            WasiPreview1HostFunction.FD_ADVISE -> ::FdAdvise
            WasiPreview1HostFunction.FD_ALLOCATE -> ::FdAllocate
            WasiPreview1HostFunction.FD_DATASYNC -> ::FdDatasync
            WasiPreview1HostFunction.FD_FDSTAT_SET_FLAGS -> ::FdFdstatSetFlags
            WasiPreview1HostFunction.FD_FDSTAT_SET_RIGHTS -> ::FdFdstatSetRights
            WasiPreview1HostFunction.FD_FILESTAT_GET -> ::FdFilestatGet
            WasiPreview1HostFunction.FD_FILESTAT_SET_SIZE -> ::FdFilestatSetSize
            WasiPreview1HostFunction.FD_FILESTAT_SET_TIMES -> ::FdFilestatSetTimes
            WasiPreview1HostFunction.FD_READDIR -> ::FdReaddir
            WasiPreview1HostFunction.FD_RENUMBER -> ::FdRenumber
            WasiPreview1HostFunction.FD_TELL -> ::FdTell
            WasiPreview1HostFunction.PATH_CREATE_DIRECTORY -> ::PathCreateDirectory
            WasiPreview1HostFunction.PATH_FILESTAT_GET -> ::PathFilestatGet
            WasiPreview1HostFunction.PATH_FILESTAT_SET_TIMES -> ::PathFilestatSetTimes
            WasiPreview1HostFunction.PATH_LINK -> ::PathLink
            WasiPreview1HostFunction.PATH_READLINK -> ::PathReadlink
            WasiPreview1HostFunction.PATH_REMOVE_DIRECTORY -> ::PathRemoveDirectory
            WasiPreview1HostFunction.PATH_RENAME -> ::PathRename
            WasiPreview1HostFunction.PATH_SYMLINK -> ::PathSymlink
            WasiPreview1HostFunction.PATH_UNLINK_FILE -> ::PathUnlinkFile
            WasiPreview1HostFunction.POLL_ONEOFF -> ::PollOneoff
            WasiPreview1HostFunction.PROC_EXIT -> ::ProcExit
            WasiPreview1HostFunction.PROC_RAISE -> ::ProcRaise
            WasiPreview1HostFunction.SOCK_ACCEPT -> ::SockAccept
            WasiPreview1HostFunction.SOCK_RECV -> ::SockRecv
            WasiPreview1HostFunction.SOCK_SEND -> ::SockSend
            WasiPreview1HostFunction.SOCK_SHUTDOWN -> ::SockShutdown
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
