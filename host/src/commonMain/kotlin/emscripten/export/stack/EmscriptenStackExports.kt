/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("PropertyName", "VariableNaming")

package at.released.weh.host.emscripten.export.stack

import at.released.weh.host.base.binding.WasmFunctionBinding

/**
 * Interface for calling exported functions related to the Emscripten stack
 */
public interface EmscriptenStackExports {
    public var __stack_pointer: Int
    public var __stack_end: Int?
    public var __stack_base: Int?

    /**
     * Internal Binaryen function to set stack limits
     *
     * See [emscripten/issues/16496](https://github.com/emscripten-core/emscripten/issues/16496)
     */
    public val __set_stack_limits: WasmFunctionBinding?
    public val emscripten_stack_init: WasmFunctionBinding?
    public val emscripten_stack_get_free: WasmFunctionBinding?
    public val emscripten_stack_get_base: WasmFunctionBinding?
    public val emscripten_stack_get_end: WasmFunctionBinding?
    public val emscripten_stack_get_current: WasmFunctionBinding

    /**
     * Internal Emscripten function.
     *
     * Initialize Emscripten stack globals: __stack_base, __stack_end
     *
     * ```asm
     * .functype emscripten_stack_set_limits (PTR, PTR) -> ()
     * ```
     *
     * See [stack_limits.S](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/compiler-rt/stack_limits.S)
     */
    public val emscripten_stack_set_limits: WasmFunctionBinding
    public val _emscripten_stack_alloc: WasmFunctionBinding
    public val _emscripten_stack_restore: WasmFunctionBinding
}
