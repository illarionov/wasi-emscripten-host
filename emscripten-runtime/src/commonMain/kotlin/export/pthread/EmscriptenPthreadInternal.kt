/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export.pthread

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.emcripten.runtime.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

@InternalWasiEmscriptenHostApi
public class EmscriptenPthreadInternal(
    private val exports: EmscriptenPthreadExports,
) {
    /**
     * Runs `_emscripten_tls_init` export to initialize thread-local storage
     */
    public fun emscriptenTlsInit() {
        exports._emscripten_tls_init.executeVoid()
    }

    /**
     * Runs `_emscripten_thread_init` export to initialize thread.
     *
     * @see EmscriptenPthreadExports._emscripten_thread_init
     */
    public fun emscriptenThreadInit(
        @IntWasmPtr(StructPthread::class) threadPtr: WasmPtr,
        isMain: Boolean,
        isRuntime: Boolean = true, //
        canBlock: Boolean = true, // !ENVIRONMENT_IS_WEB
        defaultStackSize: Int = DEFAULT_THREAD_STACK_SIZE,
        startProfiling: Boolean = false,
    ) {
        exports._emscripten_thread_init.executeVoid(
            threadPtr,
            isMain.toInt(),
            isRuntime.toInt(),
            canBlock.toInt(),
            defaultStackSize,
            startProfiling.toInt(),
        )
    }

    /**
     * Runs `_emscripten_thread_exit` export.
     *
     * @see EmscriptenPthreadExports._emscripten_thread_exit
     */
    public fun emscriptenThreadExit(@IntWasmPtr result: WasmPtr) {
        exports._emscripten_thread_exit.executeVoid(result)
    }

    /**
     * Runs `_emscripten_thread_free_data` export.
     *
     * @see EmscriptenPthreadExports._emscripten_thread_free_data
     */
    public fun emscriptenThreadFreeData(@IntWasmPtr(StructPthread::class) thread: WasmPtr) {
        exports._emscripten_thread_free_data.executeVoid(thread)
    }

    /**
     * Runs `_emscripten_thread_crashed` export.
     *
     * @see EmscriptenPthreadExports._emscripten_thread_crashed
     */
    public fun emscriptenThreadCrashed() {
        exports._emscripten_thread_crashed.executeVoid()
    }

    internal companion object {
        internal const val DEFAULT_THREAD_STACK_SIZE = 524288

        private fun Boolean.toInt(
            trueValue: Int = 1,
            falseValue: Int = 0,
        ) = if (this) trueValue else falseValue
    }
}
