/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten

import at.released.weh.bindings.chicory.ext.wasmValueTypeToChicoryValueType
import at.released.weh.bindings.chicory.host.module.emscripten.function.AbortJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.AssertFail
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenAsmConstAsyncOnMainThread
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenAsmConstInt
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenConsoleError
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenDateNow
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenGetNow
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenGetNowIsMonotonic
import at.released.weh.bindings.chicory.host.module.emscripten.function.EmscriptenResizeHeap
import at.released.weh.bindings.chicory.host.module.emscripten.function.Getentropy
import at.released.weh.bindings.chicory.host.module.emscripten.function.HandleStackOverflow
import at.released.weh.bindings.chicory.host.module.emscripten.function.LocaltimeJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.MmapJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.MunmapJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.NotImplemented
import at.released.weh.bindings.chicory.host.module.emscripten.function.SetittimerJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallChmod
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFaccessat
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFchmod
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFchown32
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFcntl64
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFdatasync
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFstat64
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallFtruncate64
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallGetcwd
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallMkdirat
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallOpenat
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallReadlinkat
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallRmdir
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallUnlinkat
import at.released.weh.bindings.chicory.host.module.emscripten.function.SyscallUtimensat
import at.released.weh.bindings.chicory.host.module.emscripten.function.TzsetJs
import at.released.weh.bindings.chicory.host.module.emscripten.function.syscallLstat64
import at.released.weh.bindings.chicory.host.module.emscripten.function.syscallStat64
import at.released.weh.bindings.chicory.memory.ChicoryMemoryProvider
import at.released.weh.emcripten.runtime.EmscriptenHostFunction
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.WasmModules.ENV_MODULE_NAME
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.runtime.HostFunction as ChicoryHostFunction

internal class EmscriptenEnvFunctionsBuilder(
    private val memoryProvider: ChicoryMemoryProvider,
    private val host: EmbedderHost,
    private val stackBindingsRef: () -> EmscriptenStack,
) {
    fun asChicoryHostFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): List<ChicoryHostFunction> {
        return EmscriptenHostFunction.entries.map { emscriptenFunc ->
            ChicoryHostFunction(
                moduleName,
                emscriptenFunc.wasmName,
                emscriptenFunc.type.paramTypes.map(::wasmValueTypeToChicoryValueType),
                emscriptenFunc.type.returnTypes.map(::wasmValueTypeToChicoryValueType),
                emscriptenFunc.createChicoryEmscriptenFunction(host, memoryProvider, stackBindingsRef),
            )
        }
    }
}

@Suppress("CyclomaticComplexMethod")
private fun EmscriptenHostFunction.createChicoryEmscriptenFunction(
    host: EmbedderHost,
    memoryProvider: ChicoryMemoryProvider,
    stackBindingsRef: () -> EmscriptenStack,
): WasmFunctionHandle = when (this) {
    EmscriptenHostFunction.ABORT_JS -> AbortJs(host)
    EmscriptenHostFunction.ASSERT_FAIL -> AssertFail(host, memoryProvider)
    EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_ASYNC_ON_MAIN_THREAD -> EmscriptenAsmConstAsyncOnMainThread(host)
    EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_INT -> EmscriptenAsmConstInt(host)
    EmscriptenHostFunction.EMSCRIPTEN_CONSOLE_ERROR -> EmscriptenConsoleError(host, memoryProvider)
    EmscriptenHostFunction.EMSCRIPTEN_DATE_NOW -> EmscriptenDateNow(host)
    EmscriptenHostFunction.EMSCRIPTEN_GET_NOW -> EmscriptenGetNow(host)
    EmscriptenHostFunction.EMSCRIPTEN_GET_NOW_IS_MONOTONIC -> EmscriptenGetNowIsMonotonic(host)
    EmscriptenHostFunction.EMSCRIPTEN_RESIZE_HEAP -> EmscriptenResizeHeap(host)
    EmscriptenHostFunction.GETENTROPY -> Getentropy(host, memoryProvider)
    EmscriptenHostFunction.HANDLE_STACK_OVERFLOW -> HandleStackOverflow(host, stackBindingsRef)
    EmscriptenHostFunction.LOCALTIME_JS -> LocaltimeJs(host, memoryProvider)
    EmscriptenHostFunction.MMAP_JS -> MmapJs(host)
    EmscriptenHostFunction.MUNMAP_JS -> MunmapJs(host)
    EmscriptenHostFunction.SETITIMER_JS -> SetittimerJs(host)
    EmscriptenHostFunction.SYSCALL_CHMOD -> SyscallChmod(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_FACCESSAT -> SyscallFaccessat(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_FCHMOD -> SyscallFchmod(host)
    EmscriptenHostFunction.SYSCALL_FCHOWN32 -> SyscallFchown32(host)
    EmscriptenHostFunction.SYSCALL_FCNTL64 -> SyscallFcntl64(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_FDATASYNC -> SyscallFdatasync(host)
    EmscriptenHostFunction.SYSCALL_FSTAT64 -> SyscallFstat64(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_FTRUNCATE64 -> SyscallFtruncate64(host)
    EmscriptenHostFunction.SYSCALL_GETCWD -> SyscallGetcwd(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_IOCTL -> NotImplemented
    EmscriptenHostFunction.SYSCALL_LSTAT64 -> syscallLstat64(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_MKDIRAT -> SyscallMkdirat(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_NEWFSTATAT -> NotImplemented
    EmscriptenHostFunction.SYSCALL_OPENAT -> SyscallOpenat(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_READLINKAT -> SyscallReadlinkat(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_RMDIR -> SyscallRmdir(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_STAT64 -> syscallStat64(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_UNLINKAT -> SyscallUnlinkat(host, memoryProvider)
    EmscriptenHostFunction.SYSCALL_UTIMENSAT -> SyscallUtimensat(host, memoryProvider)
    EmscriptenHostFunction.TZSET_JS -> TzsetJs(host, memoryProvider)
    EmscriptenHostFunction.EMSCRIPTEN_CHECK_BLOCKING_ALLOWED,
    EmscriptenHostFunction.EMSCRIPTEN_EXIT_WITH_LIVE_RUNTIME,
    EmscriptenHostFunction.EMSCRIPTEN_INIT_MAIN_THREAD_JS,
    EmscriptenHostFunction.EMSCRIPTEN_NOTIFY_MAILBOX_POSTMESSAGE,
    EmscriptenHostFunction.EMSCRIPTEN_RECEIVE_ON_MAIN_THREAD_JS,
    EmscriptenHostFunction.EMSCRIPTEN_THREAD_CLEANUP,
    EmscriptenHostFunction.EMSCRIPTEN_THREAD_MAILBOX_AWAIT,
    EmscriptenHostFunction.EMSCRIPTEN_THREAD_SET_STRONGREF,
    EmscriptenHostFunction.EMSCRIPTEN_UNWIND_TO_JS_EVENT_LOOP,
    EmscriptenHostFunction.EXIT,
    EmscriptenHostFunction.PTHREAD_CREATE_JS,
        -> NotImplemented
}
