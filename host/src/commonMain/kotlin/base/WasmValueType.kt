/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import kotlin.jvm.JvmInline

// https://webassembly.github.io/spec/core/appendix/index-types.html
@JvmInline
public value class WasmValueType(
    public val opcode: Byte?,
) {
    @Suppress("VARIABLE_NAME_INCORRECT")
    public companion object WebAssemblyTypes {
        public val I32: WasmValueType = WasmValueType(0x7f)
        public val I64: WasmValueType = WasmValueType(0x7e)
        public val F32: WasmValueType = WasmValueType(0x7d)
        public val F64: WasmValueType = WasmValueType(0x7c)
        public val V128: WasmValueType = WasmValueType(0x7b)
        public val FuncRef: WasmValueType = WasmValueType(0x70)
        public val ExternRef: WasmValueType = WasmValueType(0x6f)
        public val FunctionType: WasmValueType = WasmValueType(0x60)
        public val ResultType: WasmValueType = WasmValueType(0x40)
    }
}
