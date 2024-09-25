/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VARIABLE_NAME_INCORRECT")

package at.released.weh.wasi.preview1

import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32
import at.released.weh.wasm.core.WasmValueTypes.I64

/**
 * Type names used by low-level WASI interfaces.
 * https://raw.githubusercontent.com/WebAssembly/WASI/main/legacy/preview1/witx/typenames.witx
 */
public object WasiValueTypes {
    @WasmValueType
    public const val U8: Int = I32

    @WasmValueType
    public const val U16: Int = I32

    @WasmValueType
    public const val S32: Int = I32

    @WasmValueType
    public const val U32: Int = I32

    @WasmValueType
    public const val S64: Int = I64

    @WasmValueType
    public const val U64: Int = I64

    @WasmValueType
    public const val HANDLE: Int = I32
}
