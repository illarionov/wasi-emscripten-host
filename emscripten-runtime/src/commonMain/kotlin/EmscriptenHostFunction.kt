/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.emcripten.runtime

import at.released.weh.wasi.preview1.WasiValueTypes.HANDLE
import at.released.weh.wasi.preview1.WasiValueTypes.U8
import at.released.weh.wasm.core.HostFunction
import at.released.weh.wasm.core.HostFunction.HostFunctionType
import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.F64
import at.released.weh.wasm.core.WasmValueTypes.I32
import at.released.weh.wasm.core.WasmValueTypes.I64
import at.released.weh.wasm.core.witxPointer

public enum class EmscriptenHostFunction(
    public override val wasmName: String,
    public override val type: HostFunctionType,
) : HostFunction {
    ABORT_JS(
        wasmName = "_abort_js",
        paramTypes = listOf(),
    ),
    ASSERT_FAIL(
        wasmName = "__assert_fail",
        paramTypes = listOf(
            witxPointer(U8), // pCondition
            witxPointer(U8), // filename
            I32, // line
            witxPointer(U8), // func
        ),
    ),
    EMSCRIPTEN_ASM_CONST_ASYNC_ON_MAIN_THREAD(
        wasmName = "emscripten_asm_const_async_on_main_thread",
        paramTypes = listOf(
            witxPointer(U8), // emAsmAddr
            witxPointer(U8), // sigPtr
            witxPointer(U8), // argbuf,
        ),
    ),
    EMSCRIPTEN_ASM_CONST_INT(
        wasmName = "emscripten_asm_const_int",
        paramTypes = listOf(
            witxPointer(U8), // emAsmAddr
            witxPointer(U8), // sigPtr
            witxPointer(U8), // argbuf
        ),
        retType = I32,
    ),
    EMSCRIPTEN_CHECK_BLOCKING_ALLOWED(
        wasmName = "emscripten_check_blocking_allowed",
        paramTypes = listOf(),
    ),
    EMSCRIPTEN_CONSOLE_ERROR(
        wasmName = "emscripten_console_error",
        paramTypes = listOf(witxPointer(U8)),
    ),
    EMSCRIPTEN_DATE_NOW(
        wasmName = "emscripten_date_now",
        paramTypes = listOf(),
        retType = F64,
    ),
    EMSCRIPTEN_EXIT_WITH_LIVE_RUNTIME(
        wasmName = "emscripten_exit_with_live_runtime",
        paramTypes = listOf(),
    ),
    EMSCRIPTEN_GET_NOW(
        wasmName = "emscripten_get_now",
        paramTypes = listOf(),
        retType = F64,
    ),
    EMSCRIPTEN_GET_NOW_IS_MONOTONIC(
        wasmName = "_emscripten_get_now_is_monotonic",
        paramTypes = listOf(),
        retType = I32,
    ),
    EMSCRIPTEN_INIT_MAIN_THREAD_JS(
        wasmName = "_emscripten_init_main_thread_js",
        paramTypes = listOf(I32),
    ),
    EMSCRIPTEN_NOTIFY_MAILBOX_POSTMESSAGE(
        wasmName = "_emscripten_notify_mailbox_postmessage",
        paramTypes = listOf(I32, I32),
    ),
    EMSCRIPTEN_RECEIVE_ON_MAIN_THREAD_JS(
        wasmName = "_emscripten_receive_on_main_thread_js",
        paramTypes = List(5) { I32 },
        retType = F64,
    ),
    EMSCRIPTEN_RESIZE_HEAP(
        wasmName = "emscripten_resize_heap",
        paramTypes = listOf(
            I32, // requestedSize
        ),
        retType = I32,
    ),
    EMSCRIPTEN_RUNTIME_KEEPALIVE_CLEAR(
        wasmName = "_emscripten_runtime_keepalive_clear",
        paramTypes = listOf(),
    ),
    EMSCRIPTEN_THREAD_CLEANUP(
        wasmName = "_emscripten_thread_cleanup",
        paramTypes = listOf(I32),
    ),
    EMSCRIPTEN_THREAD_MAILBOX_AWAIT(
        wasmName = "_emscripten_thread_mailbox_await",
        paramTypes = listOf(I32),
    ),
    EMSCRIPTEN_THREAD_SET_STRONGREF(
        wasmName = "_emscripten_thread_set_strongref",
        paramTypes = listOf(I32),
    ),
    EMSCRIPTEN_UNWIND_TO_JS_EVENT_LOOP(
        wasmName = "emscripten_unwind_to_js_event_loop",
        paramTypes = listOf(),
    ),
    EXIT(
        wasmName = "exit",
        paramTypes = listOf(I32),
    ),
    GETENTROPY(
        wasmName = "getentropy",
        paramTypes = listOf(
            witxPointer(U8), // buffer
            I32, // size
        ),
        retType = I32,
    ),
    HANDLE_STACK_OVERFLOW(
        wasmName = "__handle_stack_overflow",
        paramTypes = listOf(
            I32, // requested
        ),
    ),
    LOCALTIME_JS(
        wasmName = "_localtime_js",
        paramTypes = listOf(I64, I32),
    ),
    MMAP_JS(
        wasmName = "_mmap_js",
        paramTypes = listOf(I32, I32, I32, I32, I64, I32, I32),
        retType = I32,
    ),
    MUNMAP_JS(
        wasmName = "_munmap_js",
        paramTypes = listOf(I32, I32, I32, I32, I32, I64),
        retType = I32,
    ),
    PTHREAD_CREATE_JS(
        wasmName = "__pthread_create_js",
        paramTypes = List(4) { I32 },
        retType = I32,
    ),
    SETITIMER_JS(
        wasmName = "_setitimer_js",
        paramTypes = listOf(I32, F64),
        retType = I32,
    ),
    SYSCALL_CHMOD(
        wasmName = "__syscall_chmod",
        paramTypes = listOf(I32, I32),
        retType = I32,
    ),
    SYSCALL_FACCESSAT(
        wasmName = "__syscall_faccessat",
        paramTypes = List(4) { I32 },
        retType = I32,
    ),
    SYSCALL_FCHMOD(
        wasmName = "__syscall_fchmod",
        paramTypes = listOf(I32, I32),
        retType = I32,
    ),
    SYSCALL_FCHOWN32(
        wasmName = "__syscall_fchown32",
        paramTypes = listOf(
            HANDLE, // fd
            I32, // owner,
            I32, // group,
        ),
        retType = I32,
    ),
    SYSCALL_FCNTL64(
        wasmName = "__syscall_fcntl64",
        paramTypes = listOf(
            I32,
            I32, // owner,
            I32, // group,
        ),
        retType = I32,
    ),
    SYSCALL_FDATASYNC(
        wasmName = "__syscall_fdatasync",
        paramTypes = listOf(I32),
        retType = I32,
    ),
    SYSCALL_FSTAT64(
        wasmName = "__syscall_fstat64",
        paramTypes = listOf(
            HANDLE, // fd
            witxPointer(U8), // statbuf
        ),
        retType = I32,
    ),
    SYSCALL_FTRUNCATE64(
        wasmName = "__syscall_ftruncate64",
        paramTypes = listOf(I32, I64),
        retType = I32,
    ),
    SYSCALL_GETCWD(
        wasmName = "__syscall_getcwd",
        paramTypes = listOf(
            witxPointer(U8), // buf
            I32, // size
        ),
        retType = I32,
    ),
    SYSCALL_IOCTL(
        wasmName = "__syscall_ioctl",
        paramTypes = List(3) { I32 },
        retType = I32,
    ),
    SYSCALL_LSTAT64(
        wasmName = "__syscall_lstat64",
        paramTypes = listOf(I32, I32),
        retType = I32,
    ),
    SYSCALL_MKDIRAT(
        wasmName = "__syscall_mkdirat",
        paramTypes = List(3) { I32 },
        retType = I32,
    ),
    SYSCALL_NEWFSTATAT(
        wasmName = "__syscall_newfstatat",
        paramTypes = List(4) { I32 },
        retType = I32,
    ),
    SYSCALL_OPENAT(
        wasmName = "__syscall_openat",
        paramTypes = listOf(
            HANDLE, // dirfd
            witxPointer(U8), // pathname
            I32, // flags
            I32, // mode / varargs
        ),
        retType = I32,
    ),
    SYSCALL_READLINKAT(
        wasmName = "__syscall_readlinkat",
        paramTypes = listOf(
            HANDLE, // dirfd
            witxPointer(U8), // pathname
            witxPointer(U8), // buf
            I32, // bufsiz
        ),
        retType = I32,
    ),
    SYSCALL_RMDIR(
        wasmName = "__syscall_rmdir",
        paramTypes = listOf(I32),
        retType = I32,
    ),
    SYSCALL_STAT64(
        wasmName = "__syscall_stat64",
        paramTypes = listOf(I32, I32),
        retType = I32,
    ),
    SYSCALL_UNLINKAT(
        wasmName = "__syscall_unlinkat",
        paramTypes = List(3) { I32 },
        retType = I32,
    ),
    SYSCALL_UTIMENSAT(
        wasmName = "__syscall_utimensat",
        paramTypes = listOf(
            I32, // dirfd
            witxPointer(U8), // pathname
            witxPointer(U8), // times
            I32, // flags
        ),
        retType = I32,
    ),
    TZSET_JS(
        wasmName = "_tzset_js",
        paramTypes = List(4) { I32 },
    ),
    ;

    constructor(
        wasmName: String,
        @WasmValueType paramTypes: List<Int>,
        @WasmValueType retType: Int? = null,
    ) : this(
        wasmName = wasmName,
        type = HostFunctionType(
            params = paramTypes,
            returnTypes = if (retType != null) {
                listOf(retType)
            } else {
                emptyList()
            },
        ),
    )

    public companion object {
        public val byWasmName: Map<String, EmscriptenHostFunction> = entries
            .associateBy(EmscriptenHostFunction::wasmName)
    }
}
