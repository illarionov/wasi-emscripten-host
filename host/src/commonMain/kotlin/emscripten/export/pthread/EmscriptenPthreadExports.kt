/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VariableNaming", "PropertyName")

package at.released.weh.host.emscripten.export.pthread

import at.released.weh.host.base.binding.WasmFunctionBinding

public interface EmscriptenPthreadExports {
    public var __tls_base: Int
    public val __tls_size: Int
    public val __tls_align: Int

    /**
     * Internal Emscripten thread-local wasm global.
     *
     * Signal that the current thread is non to use the primary/static TLS region.
     * Once this gets set it forces that all future calls to emscripten_tls_init to dynamically allocate TLS.
     * If this global is true then TLS needs to be dyanically allocated, if its
     * false we are free to use the existing/global __tls_base.
     *
     * Initialized in [_emscripten_tls_init]
     *
     * See [emscripten_tls_init.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_tls_init.c)
     */
    public var g_needs_dynamic_alloc: Int

    /**
     * Internal Emscripten thread-local wasm global.
     *
     * Part of the thread state. Initialized (in `__set_thread_state()`) from values passed to [_emscripten_thread_init]
     *
     * See [emscripten_thread_state.S](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_thread_state.S)
     */
    public var thread_ptr: Int

    /**
     * Internal Emscripten thread-local wasm global.
     *
     * Semantically the same as testing "!ENVIRONMENT_IS_WORKER" in JS
     *
     * Part of the thread state. Initialized (in `__set_thread_state()`) from values passed to [_emscripten_thread_init]
     *
     * See [emscripten_thread_state.S](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_thread_state.S)
     */
    public var is_main_thread: Int

    /**
     * Internal Emscripten thread-local wasm global.
     *
     * Semantically the same as testing "!ENVIRONMENT_IS_PTHREAD" in JS
     *
     * Part of the thread state. Initialized (in `__set_thread_state()`) from values passed to [_emscripten_thread_init]
     *
     * See [emscripten_thread_state.S](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_thread_state.S)
     */
    public var is_runtime_thread: Int

    /**
     * Internal Emscripten thread-local wasm global.
     *
     * Semantically the same as testing "!ENVIRONMENT_IS_WEB" in JS
     *
     * Non-zero if the calling thread supports Atomic.wait (For example if called from the main browser thread,
     * this function will return zero since blocking is not allowed there)
     *
     * Part of the thread state. Initialized (in `__set_thread_state()`) from values passed to [_emscripten_thread_init]
     *
     * See [emscripten_thread_state.S](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_thread_state.S)
     */
    public var supports_wait: Int

    /**
     * Internal Emscripten function, called from runtime_pthread.js
     *
     * `void _emscripten_check_mailbox()`
     *
     * [thread_mailbox.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/thread_mailbox.c#L74)
     */
    public val _emscripten_check_mailbox: WasmFunctionBinding

    /**
     * Internal Emscripten function
     *
     * ```c
     *  double _emscripten_run_on_main_thread_js(int func_index,
     *                                           void* em_asm_addr,
     *                                           int num_args,
     *                                           double* buffer,
     *                                           int sync)
     * ```
     *
     * [proxying.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/proxying.c)
     */
    public val _emscripten_run_on_main_thread_js: WasmFunctionBinding

    /**
     * Called when an exception is thrown by JavaScript code in a worker.
     *
     * Internal Emscripten function.
     *
     * ```c
     * void _emscripten_thread_crashed()
     * ```
     */
    public val _emscripten_thread_crashed: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * Called from a Web Worker when a pthread is detached from the web worker.
     * Runs thread destructors and internal cleanup, does not release StructPthread.
     *
     * ```c
     * void _emscripten_thread_exit(void* result)
     * ```
     *
     */
    public val _emscripten_thread_exit: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * Called from JS main thread to free data associated a thread that is no longer running.
     *
     * Should not be called from the thread being deallocated
     *
     * ```c
     * void _emscripten_thread_free_data(pthread_t t)
     * ```
     *
     * See [pthread_create.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/pthread_create.c)
     */
    public val _emscripten_thread_free_data: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * Initializes Wasm globals for thread metadata using the provided values and sets the thread status to RUNNING
     *
     * ```c
     * void _emscripten_thread_init(pthread_t ptr,
     *                              int is_main,
     *                              int is_runtime,
     *                              int can_block,
     *                              int default_stacksize,
     *                              int start_profiling)
     * ```
     *
     * See [emscripten_thread_init.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_thread_init.c)
     */
    public val _emscripten_thread_init: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * Allocates memory (for worker thread) and initializes thread-local storage including the thread stack
     *
     * ```c
     * void* _emscripten_tls_init(void)
     * ```
     *
     * See [emscripten_tls_init.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_tls_init.c)
     */
    public val _emscripten_tls_init: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * ```c
     * pthread_t emscripten_main_runtime_thread_id() {
     *   return &__main_pthread;
     * }
     * ```
     *
     * See [pthread_self_stub.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/pthread_self_stub.c)
     */
    public val emscripten_main_runtime_thread_id: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * ```c
     * void emscripten_main_thread_process_queued_calls()
     * ```
     *
     * See [library_pthread.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/library_pthread.c)
     */
    public val emscripten_main_thread_process_queued_calls: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * MUSL `pthread_t pthread_self(void)`. Returns [thread_ptr] global.
     */
    public val pthread_self: WasmFunctionBinding

    /**
     * POSIX pthread_create() (if available)
     */
    public val pthread_create: WasmFunctionBinding?

    /**
     * POSIX pthread_exit() (if available)
     */
    public val pthread_exit: WasmFunctionBinding?
}
