/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VARIABLE_NAME_INCORRECT")

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType

/**
 * Type names used by low-level WASI interfaces.
 * https://raw.githubusercontent.com/WebAssembly/WASI/main/legacy/preview1/witx/typenames.witx
 */
public object WasiValueTypes {
    public val U8: WasmValueType = WasmValueType.I32
    public val U16: WasmValueType = WasmValueType.I32
    public val S32: WasmValueType = WasmValueType.I32
    public val U32: WasmValueType = WasmValueType.I32
    public val S64: WasmValueType = WasmValueType.I64
    public val U64: WasmValueType = WasmValueType.I64
    public val Handle: WasmValueType = WasmValueType.I32
}
