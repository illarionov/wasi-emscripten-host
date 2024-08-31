/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NoMultipleSpaces")

package at.released.weh.bindings.chicory.host.module.emscripten

import at.released.weh.bindings.chicory.ext.chicory
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
import at.released.weh.bindings.chicory.host.module.emscripten.function.notImplementedEmscriptenHostFunction
import at.released.weh.bindings.chicory.host.module.emscripten.function.syscallLstat64
import at.released.weh.bindings.chicory.host.module.emscripten.function.syscallStat64
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmModules.ENV_MODULE_NAME
import at.released.weh.host.base.WasmValueType
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.emscripten.export.stack.EmscriptenStack
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.wasm.types.Value
import com.dylibso.chicory.runtime.HostFunction as ChicoryHostFunction

internal class EmscriptenEnvFunctionsBuilder(
    private val memory: Memory,
    private val host: EmbedderHost,
    private val stackBindingsRef: () -> EmscriptenStack,
) {
    private val EmscriptenHostFunction.functionFactory: EmscriptenHostFunctionHandleFactory
        get() = when (this) {
            EmscriptenHostFunction.ABORT_JS -> { host, _ -> AbortJs(host) }
            EmscriptenHostFunction.ASSERT_FAIL -> ::AssertFail
            EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_ASYNC_ON_MAIN_THREAD -> { host, _ ->
                EmscriptenAsmConstAsyncOnMainThread(host)
            }

            EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_INT -> { host, _ -> EmscriptenAsmConstInt(host) }
            EmscriptenHostFunction.EMSCRIPTEN_CONSOLE_ERROR -> ::EmscriptenConsoleError
            EmscriptenHostFunction.EMSCRIPTEN_DATE_NOW -> { host, _ -> EmscriptenDateNow(host) }
            EmscriptenHostFunction.EMSCRIPTEN_GET_NOW -> { host, _ -> EmscriptenGetNow(host) }
            EmscriptenHostFunction.EMSCRIPTEN_GET_NOW_IS_MONOTONIC -> { host, _ -> EmscriptenGetNowIsMonotonic(host) }
            EmscriptenHostFunction.EMSCRIPTEN_RESIZE_HEAP -> { host, _ -> EmscriptenResizeHeap(host) }
            EmscriptenHostFunction.GETENTROPY -> ::Getentropy
            EmscriptenHostFunction.HANDLE_STACK_OVERFLOW -> { host, _ -> HandleStackOverflow(host, stackBindingsRef) }
            EmscriptenHostFunction.LOCALTIME_JS -> ::LocaltimeJs
            EmscriptenHostFunction.MMAP_JS -> { host, _ -> MmapJs(host) }
            EmscriptenHostFunction.MUNMAP_JS -> { host, _ -> MunmapJs(host) }
            EmscriptenHostFunction.SYSCALL_CHMOD -> ::SyscallChmod
            EmscriptenHostFunction.SYSCALL_FACCESSAT -> ::SyscallFaccessat
            EmscriptenHostFunction.SYSCALL_FCHMOD -> { host, _ -> SyscallFchmod(host) }
            EmscriptenHostFunction.SYSCALL_FCHOWN32 -> { host, _ -> SyscallFchown32(host) }
            EmscriptenHostFunction.SYSCALL_FCNTL64 -> ::SyscallFcntl64
            EmscriptenHostFunction.SYSCALL_FDATASYNC -> { host, _ -> SyscallFdatasync(host) }
            EmscriptenHostFunction.SYSCALL_FSTAT64 -> ::SyscallFstat64
            EmscriptenHostFunction.SYSCALL_FTRUNCATE64 -> { host, _ -> SyscallFtruncate64(host) }
            EmscriptenHostFunction.SYSCALL_GETCWD -> ::SyscallGetcwd
            EmscriptenHostFunction.SYSCALL_IOCTL -> notImplementedEmscriptenHostFunction
            EmscriptenHostFunction.SYSCALL_LSTAT64 -> ::syscallLstat64
            EmscriptenHostFunction.SYSCALL_MKDIRAT -> ::SyscallMkdirat
            EmscriptenHostFunction.SYSCALL_NEWFSTATAT -> notImplementedEmscriptenHostFunction
            EmscriptenHostFunction.SYSCALL_OPENAT -> ::SyscallOpenat
            EmscriptenHostFunction.SYSCALL_READLINKAT -> ::SyscallReadlinkat
            EmscriptenHostFunction.SYSCALL_RMDIR -> ::SyscallRmdir
            EmscriptenHostFunction.SYSCALL_STAT64 -> ::syscallStat64
            EmscriptenHostFunction.SYSCALL_UNLINKAT -> ::SyscallUnlinkat
            EmscriptenHostFunction.SYSCALL_UTIMENSAT -> ::SyscallUtimensat
            EmscriptenHostFunction.TZSET_JS -> ::TzsetJs
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
                -> notImplementedEmscriptenHostFunction
        }

    fun asChicoryHostFunctions(
        moduleName: String = ENV_MODULE_NAME,
    ): List<ChicoryHostFunction> {
        return EmscriptenHostFunction.entries.map { emscriptenFunc ->
            ChicoryHostFunction(
                HostFunctionAdapter(emscriptenFunc.functionFactory(host, memory)),
                moduleName,
                emscriptenFunc.wasmName,
                emscriptenFunc.type.paramTypes.map(WasmValueType::chicory),
                emscriptenFunc.type.returnTypes.map(WasmValueType::chicory),
            )
        }
    }

    private class HostFunctionAdapter(
        private val delegate: EmscriptenHostFunctionHandle,
    ) : WasmFunctionHandle {
        override fun apply(instance: Instance, vararg args: Value): Array<Value> {
            val result: Value? = delegate.apply(instance, args = args)
            return if (result != null) {
                arrayOf(result)
            } else {
                arrayOf()
            }
        }
    }
}
