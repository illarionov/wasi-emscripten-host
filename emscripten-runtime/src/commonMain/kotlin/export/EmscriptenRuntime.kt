/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export

/**
 * Emscripten environment manager
 */
public interface EmscriptenRuntime {
    /**
     * Whether environment supports multithreading
     */
    public val isMultiThread: Boolean

    /**
     * Emscripten WebAssembly start function.
     *
     * This function should be invoked first after the module instantiation if the WebAssembly binary was not compiled
     * in standalone mode. In standalone mode, the "initialize" exported function should be used instead.
     *
     * Calls `__wasm_call_ctors` exported function under the hood.
     *
     * Emscripten internal static constructors called from this method: [system/lib/README.md](https://github.com/emscripten-core/emscripten/blob/16a0bf174cb85f88b6d9dcc8ee7fbca59390185b/system/lib/README.md)
     */
    public fun initMainThread()
}
