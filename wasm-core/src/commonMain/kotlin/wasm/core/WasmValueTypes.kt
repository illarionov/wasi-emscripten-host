/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VARIABLE_NAME_INCORRECT")

package at.released.weh.wasm.core

/**
 * Binary opcodes of the WebAssembly value types.
 *
 * WebAssembly value types are number types, vector types, or reference types according to the [WebAssembly types](https://webassembly.github.io/spec/core/syntax/types.html#value-types)
 *
 * See also [WebAssembly index of types](https://webassembly.github.io/spec/core/appendix/index-types.html)
 */
public object WasmValueTypes {
    @WasmValueType
    public const val I32: Int = 0x7f

    @WasmValueType
    public const val I64: Int = 0x7e

    @WasmValueType
    public const val F32: Int = 0x7d

    @WasmValueType
    public const val F64: Int = 0x7c

    @WasmValueType
    public const val V128: Int = 0x7b

    @WasmValueType
    public const val FUNC_REF: Int = 0x70

    @WasmValueType
    public const val EXTERN_REF: Int = 0x6f
}
