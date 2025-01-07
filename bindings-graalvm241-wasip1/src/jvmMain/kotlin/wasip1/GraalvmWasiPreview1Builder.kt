/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.wasip1

import at.released.weh.bindings.graalvm241.ext.NodeFactory
import at.released.weh.bindings.graalvm241.ext.createWasmModule
import at.released.weh.bindings.graalvm241.memory.MemorySource
import at.released.weh.bindings.graalvm241.wasip1.function.ArgsGet
import at.released.weh.bindings.graalvm241.wasip1.function.ArgsSizesGet
import at.released.weh.bindings.graalvm241.wasip1.function.ClockResGet
import at.released.weh.bindings.graalvm241.wasip1.function.ClockTimeGet
import at.released.weh.bindings.graalvm241.wasip1.function.EnvironGet
import at.released.weh.bindings.graalvm241.wasip1.function.EnvironSizesGet
import at.released.weh.bindings.graalvm241.wasip1.function.FdAdvise
import at.released.weh.bindings.graalvm241.wasip1.function.FdAllocate
import at.released.weh.bindings.graalvm241.wasip1.function.FdClose
import at.released.weh.bindings.graalvm241.wasip1.function.FdDatasync
import at.released.weh.bindings.graalvm241.wasip1.function.FdFdstatGet
import at.released.weh.bindings.graalvm241.wasip1.function.FdFdstatSetFlags
import at.released.weh.bindings.graalvm241.wasip1.function.FdFdstatSetRights
import at.released.weh.bindings.graalvm241.wasip1.function.FdFilestatGet
import at.released.weh.bindings.graalvm241.wasip1.function.FdFilestatSetSize
import at.released.weh.bindings.graalvm241.wasip1.function.FdFilestatSetTimes
import at.released.weh.bindings.graalvm241.wasip1.function.FdPread
import at.released.weh.bindings.graalvm241.wasip1.function.FdPrestatDirName
import at.released.weh.bindings.graalvm241.wasip1.function.FdPrestatGet
import at.released.weh.bindings.graalvm241.wasip1.function.FdPwrite
import at.released.weh.bindings.graalvm241.wasip1.function.FdRead
import at.released.weh.bindings.graalvm241.wasip1.function.FdReaddir
import at.released.weh.bindings.graalvm241.wasip1.function.FdRenumber
import at.released.weh.bindings.graalvm241.wasip1.function.FdSeek
import at.released.weh.bindings.graalvm241.wasip1.function.FdSync
import at.released.weh.bindings.graalvm241.wasip1.function.FdTell
import at.released.weh.bindings.graalvm241.wasip1.function.FdWrite
import at.released.weh.bindings.graalvm241.wasip1.function.PathCreateDirectory
import at.released.weh.bindings.graalvm241.wasip1.function.PathFilestatGet
import at.released.weh.bindings.graalvm241.wasip1.function.PathFilestatSetTimes
import at.released.weh.bindings.graalvm241.wasip1.function.PathLink
import at.released.weh.bindings.graalvm241.wasip1.function.PathOpen
import at.released.weh.bindings.graalvm241.wasip1.function.PathReadlink
import at.released.weh.bindings.graalvm241.wasip1.function.PathRemoveDirectory
import at.released.weh.bindings.graalvm241.wasip1.function.PathRename
import at.released.weh.bindings.graalvm241.wasip1.function.PathSymlink
import at.released.weh.bindings.graalvm241.wasip1.function.PathUnlinkFile
import at.released.weh.bindings.graalvm241.wasip1.function.PollOneoff
import at.released.weh.bindings.graalvm241.wasip1.function.ProcRaise
import at.released.weh.bindings.graalvm241.wasip1.function.RandomGet
import at.released.weh.bindings.graalvm241.wasip1.function.SchedYield
import at.released.weh.bindings.graalvm241.wasip1.function.SockAccept
import at.released.weh.bindings.graalvm241.wasip1.function.SockRecv
import at.released.weh.bindings.graalvm241.wasip1.function.SockSend
import at.released.weh.bindings.graalvm241.wasip1.function.SockShutdown
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasm.core.WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME
import org.graalvm.polyglot.Context

/**
 * WASI Preview 1 host function installer.
 *
 * Sets up WebAssembly WASI Preview 1 WebAssembly module in GraalWASM [Context].
 *
 * Usage example:
 *
 * ```kotlin
 * val source = Source.newBuilder("wasm", ByteSequence.create(wasmFile), "proc").build()
 * Context.newBuilder().engine(engine).build().use { context ->
 *     context.initialize("wasm")
 *     GraalvmWasiPreview1Builder {
 *         host = embedderHost // setup host
 *     }.build(context)
 *     context.eval(source)
 *
 *     val startFunc = context.getBindings("wasm").getMember("proc").getMember("_start")
 *     startFunc.execute()
 *     try {
 *          startFunction.execute()
 *      } catch (re: PolyglotException) {
 *          if (re.message?.startsWith("Program exited with status code") == false) {
 *              throw re
 *          }
 *          Unit
 *     }
 * }
 * ```
 *
 */
@WasiEmscriptenHostDsl
public class GraalvmWasiPreview1Builder {
    /**
     * Implementation of a host object that provides access from the WebAssembly to external host resources.
     */
    @set:JvmSynthetic
    public var host: EmbedderHost? = null

    /**
     * Memory source used for all operations.
     */
    @set:JvmSynthetic
    public var memorySource: MemorySource.ImportedMemory? = MemorySource.ImportedMemory()

    /**
     * Sets implementation of a host object that provides access from the WebAssembly to external host resources.
     */
    public fun setHost(host: EmbedderHost?): GraalvmWasiPreview1Builder = apply {
        this.host = host
    }

    /**
     * Sets memory source used for all operations.
     */
    public fun setMemorySource(memorySource: MemorySource.ImportedMemory?): GraalvmWasiPreview1Builder = apply {
        this.memorySource = memorySource
    }

    @JvmOverloads
    public fun build(
        wasmContext: Context,
        moduleName: String = WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
    ) {
        createWasmModule(
            graalvmContext = wasmContext,
            moduleName = moduleName,
            memorySource = memorySource,
            host = host ?: EmbedderHostBuilder().build(),
            functions = WasiPreview1HostFunction.entries.associateWith { it.nodeFactory },
        )
    }

    public companion object {
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

        @JvmSynthetic
        public operator fun invoke(
            block: GraalvmWasiPreview1Builder.() -> Unit = {},
        ): GraalvmWasiPreview1Builder {
            return GraalvmWasiPreview1Builder().apply(block)
        }
    }
}
