/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import at.released.weh.common.api.InternalWasiEmscriptenHostApi

/**
 * WebAssembly value types.
 *
 * Number types, vector types, or reference types according to the [WebAssembly types](https://webassembly.github.io/spec/core/syntax/types.html#value-types).
 *
 * @property opcode Binary Opcode of the type (see [WebAssembly index of types](https://webassembly.github.io/spec/core/appendix/index-types.html))
 */
public enum class WasmValueType(
    public val opcode: Byte,
) {
    I32(0x7f),
    I64(0x7e),
    F32(0x7d),
    F64(0x7c),
    V128(0x7b),
    FuncRef(0x70),
    ExternRef(0x6f),
}


