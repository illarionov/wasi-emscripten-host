/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlin.jvm.JvmStatic

/**
 * C pointer in the 32-bit WebAssembly memory.
 */
public typealias WasmPtr = @IntWasmPtr Int

public object WasmPtrUtil {
    /**
     * Size of a 32-bit pointer.
     */
    public const val WASM_SIZEOF_PTR: Int = 4

    /**
     * C NULL pointer.
     */
    @IntWasmPtr
    public const val C_NULL: WasmPtr = 0

    /**
     * Checks if [ptr] is NULL.
     */
    @InternalWasiEmscriptenHostApi
    @JvmStatic
    public fun ptrIsNull(@IntWasmPtr ptr: WasmPtr): Boolean = ptr == C_NULL
}
