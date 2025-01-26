/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VariableNaming", "PropertyName")

package at.released.weh.emcripten.runtime.export.timer

import at.released.weh.wasm.core.WasmFunctionBinding

public interface IntervalTimerExports {
    /**
     * Internal Emscripten function.
     *
     * Called from JS main thread when timer set by setitimer() expires
     *
     * This function is supposed to be called from the main JS loop. Implementation is not thread-safe.
     *
     * ```c
     * void _emscripten_timeout(int which, double now)
     * ```
     *
     * * which: ITIMER_PROF, ITIMER_VIRTUAL, or ITIMER_REAL
     * * now: current timer value in milliseconds
     *
     * See [setitimer.c](https://github.com/emscripten-core/emscripten/blob/4.0.1/system/lib/libc/musl/src/signal/setitimer.c#L34)
     */
    public val _emscripten_timeout: WasmFunctionBinding?
}
