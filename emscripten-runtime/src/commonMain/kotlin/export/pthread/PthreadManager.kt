/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export.pthread

import at.released.weh.emcripten.runtime.export.pthread.EmscriptenPthreadInternal.Companion.DEFAULT_THREAD_STACK_SIZE
import at.released.weh.emcripten.runtime.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public open class PthreadManager(
    private val pthreadInternal: EmscriptenPthreadInternal,
    private val isMainThread: () -> Boolean,
) {
    /**
     * Handler for `_emscripten_init_main_thread_js`
     *
     * Called from  `__wasm_call_ctors` -> `_emscripten_init_main_thread` during the initial initialization of the
     * main thread of the webassembly environment
     */
    public fun initMainThreadJs(
        @IntWasmPtr(StructPthread::class) threadPtr: WasmPtr,
    ) {
        check(isMainThread()) { "Should be called on main thread" }

        pthreadInternal.emscriptenThreadInit(
            threadPtr = threadPtr,
            isMain = true,
            isRuntime = true,
            canBlock = true,
            defaultStackSize = DEFAULT_THREAD_STACK_SIZE,
            startProfiling = false,
        )
        pthreadInternal.emscriptenTlsInit()
    }
}
