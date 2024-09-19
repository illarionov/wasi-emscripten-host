/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller.Companion.DEFAULT_MEMORY_SPEC
import at.released.weh.bindings.graalvm241.MemorySource
import at.released.weh.bindings.graalvm241.ext.WasmModuleMemoryHelper
import at.released.weh.bindings.graalvm241.ext.setupWasmModuleFunctions
import at.released.weh.bindings.graalvm241.ext.withWasmContext
import at.released.weh.bindings.graalvm241.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.host.module.NodeFactory
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.AbortJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.AssertFail
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenAsmConstAsyncOnMainThread
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenAsmConstInt
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenConsoleError
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenDateNow
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenGetNow
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenGetNowIsMonotonic
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenInitMainThreadJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenResizeHeap
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenThreadMailboxAwait
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.Getentropy
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.HandleStackOverflow
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.LocaltimeJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.MmapJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.MunapJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallChmod
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFaccessat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFchmod
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFchown32
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFcntl64
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFstat64
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallFtruncate64
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallGetcwd
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallMkdirat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallOpenat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallReadlinkat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallRmdir
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallUnlinkat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.SyscallUtimensat
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.TzsetJs
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.syscallLstat64
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.syscallStat64
import at.released.weh.bindings.graalvm241.host.module.notImplementedFunctionNodeFactory
import at.released.weh.bindings.graalvm241.host.module.wasi.function.FdDataSync
import at.released.weh.bindings.graalvm241.host.pthread.GraalvmPthreadManager
import at.released.weh.bindings.graalvm241.host.pthread.PthreadCreateJsWasmNode
import at.released.weh.emcripten.runtime.EmscriptenHostFunction
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import org.graalvm.polyglot.Context
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class EmscriptenEnvModuleBuilder(
    private val host: EmbedderHost,
    private val pthreadRef: () -> GraalvmPthreadManager,
    private val emscriptenStackRef: () -> EmscriptenStack,
    private val memoryWaiters: SharedMemoryWaiterListStore,
) {
    private val EmscriptenHostFunction.nodeFactory: NodeFactory
        get() = when (this) {
            EmscriptenHostFunction.ABORT_JS -> ::AbortJs
            EmscriptenHostFunction.ASSERT_FAIL -> ::AssertFail
            EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_INT -> ::EmscriptenAsmConstInt
            EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_ASYNC_ON_MAIN_THREAD -> ::EmscriptenAsmConstAsyncOnMainThread
            EmscriptenHostFunction.EMSCRIPTEN_DATE_NOW -> ::EmscriptenDateNow
            EmscriptenHostFunction.EMSCRIPTEN_CONSOLE_ERROR -> ::EmscriptenConsoleError
            EmscriptenHostFunction.EMSCRIPTEN_GET_NOW -> ::EmscriptenGetNow
            EmscriptenHostFunction.EMSCRIPTEN_GET_NOW_IS_MONOTONIC -> ::EmscriptenGetNowIsMonotonic
            EmscriptenHostFunction.EMSCRIPTEN_RESIZE_HEAP -> ::EmscriptenResizeHeap
            EmscriptenHostFunction.GETENTROPY -> ::Getentropy
            EmscriptenHostFunction.HANDLE_STACK_OVERFLOW -> {
                    language: WasmLanguage,
                    module: WasmModule,
                    host: EmbedderHost,
                ->
                HandleStackOverflow(
                    language = language,
                    module = module,
                    host = host,
                    stackBindingsRef = emscriptenStackRef,
                )
            }

            EmscriptenHostFunction.LOCALTIME_JS -> ::LocaltimeJs
            EmscriptenHostFunction.MMAP_JS -> ::MmapJs
            EmscriptenHostFunction.MUNMAP_JS -> ::MunapJs
            EmscriptenHostFunction.SYSCALL_CHMOD -> ::SyscallChmod
            EmscriptenHostFunction.SYSCALL_FACCESSAT -> ::SyscallFaccessat
            EmscriptenHostFunction.SYSCALL_FCHMOD -> ::SyscallFchmod
            EmscriptenHostFunction.SYSCALL_FCHOWN32 -> ::SyscallFchown32
            EmscriptenHostFunction.SYSCALL_FCNTL64 -> ::SyscallFcntl64
            EmscriptenHostFunction.SYSCALL_FDATASYNC -> ::FdDataSync
            EmscriptenHostFunction.SYSCALL_FSTAT64 -> ::SyscallFstat64
            EmscriptenHostFunction.SYSCALL_FTRUNCATE64 -> ::SyscallFtruncate64
            EmscriptenHostFunction.SYSCALL_GETCWD -> ::SyscallGetcwd
            EmscriptenHostFunction.SYSCALL_IOCTL -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.SYSCALL_MKDIRAT -> ::SyscallMkdirat
            EmscriptenHostFunction.SYSCALL_NEWFSTATAT -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.SYSCALL_OPENAT -> ::SyscallOpenat
            EmscriptenHostFunction.SYSCALL_READLINKAT -> ::SyscallReadlinkat
            EmscriptenHostFunction.SYSCALL_RMDIR -> ::SyscallRmdir
            EmscriptenHostFunction.SYSCALL_STAT64 -> ::syscallStat64
            EmscriptenHostFunction.SYSCALL_LSTAT64 -> ::syscallLstat64
            EmscriptenHostFunction.SYSCALL_UNLINKAT -> ::SyscallUnlinkat
            EmscriptenHostFunction.SYSCALL_UTIMENSAT -> ::SyscallUtimensat
            EmscriptenHostFunction.TZSET_JS -> ::TzsetJs
            EmscriptenHostFunction.EMSCRIPTEN_THREAD_SET_STRONGREF -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_UNWIND_TO_JS_EVENT_LOOP -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_EXIT_WITH_LIVE_RUNTIME -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_INIT_MAIN_THREAD_JS -> {
                    language: WasmLanguage,
                    module: WasmModule,
                    host: EmbedderHost,
                ->
                EmscriptenInitMainThreadJs(
                    language = language,
                    module = module,
                    host = host,
                    posixThreadRef = pthreadRef,
                )
            }

            EmscriptenHostFunction.EMSCRIPTEN_THREAD_MAILBOX_AWAIT -> {
                    language: WasmLanguage,
                    module: WasmModule,
                    host: EmbedderHost,
                ->
                EmscriptenThreadMailboxAwait(
                    language = language,
                    module = module,
                    host = host,
                    posixThreadRef = pthreadRef,
                )
            }

            EmscriptenHostFunction.EMSCRIPTEN_RECEIVE_ON_MAIN_THREAD_JS -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_CHECK_BLOCKING_ALLOWED -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.PTHREAD_CREATE_JS -> {
                    language: WasmLanguage,
                    module: WasmModule,
                    host: EmbedderHost,
                ->
                PthreadCreateJsWasmNode(
                    language = language,
                    module = module,
                    host = host,
                    posixThreadRef = pthreadRef,
                )
            }

            EmscriptenHostFunction.EXIT -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_THREAD_CLEANUP -> notImplementedFunctionNodeFactory(this)
            EmscriptenHostFunction.EMSCRIPTEN_NOTIFY_MAILBOX_POSTMESSAGE -> notImplementedFunctionNodeFactory(this)
        }

    fun setupModule(
        graalContext: Context,
        moduleName: String = ENV_MODULE_NAME,
        memorySource: MemorySource = MemorySource.ImportedMemory(spec = DEFAULT_MEMORY_SPEC),
    ): WasmInstance = graalContext.withWasmContext { wasmContext ->
        val envModule = WasmModule.create(moduleName, null)
        WasmModuleMemoryHelper(envModule).setupMemory(
            source = memorySource,
            memoryWaiters = memoryWaiters,
            logger = host.rootLogger,
        )
        return setupWasmModuleFunctions(
            wasmContext,
            host,
            envModule,
            EmscriptenHostFunction.entries.associateWith { it.nodeFactory },
        )
    }
}
