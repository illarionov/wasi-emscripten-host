/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

import at.released.weh.host.base.WasmPtr
import com.dylibso.chicory.wasm.types.Value
import com.dylibso.chicory.wasm.types.Value.REF_NULL_VALUE
import com.dylibso.chicory.wasm.types.ValueType.ExternRef
import com.dylibso.chicory.wasm.types.ValueType.F32
import com.dylibso.chicory.wasm.types.ValueType.F64
import com.dylibso.chicory.wasm.types.ValueType.FuncRef
import com.dylibso.chicory.wasm.types.ValueType.I32
import com.dylibso.chicory.wasm.types.ValueType.I64
import com.dylibso.chicory.wasm.types.ValueType.UNKNOWN
import com.dylibso.chicory.wasm.types.ValueType.V128

internal fun <P : Any?> Value.asWasmAddr(): WasmPtr<P> = WasmPtr(asInt())

internal fun WasmPtr<*>.asValue(): Value = Value.i32(this.addr.toLong())

internal fun Value?.isNull(): Boolean {
    return when (this?.type()) {
        null -> true
        F64, F32, I64, I32 -> this.asLong() == -1L
        V128 -> error("Not implemented")
        FuncRef, ExternRef, UNKNOWN -> this.asExtRef() == REF_NULL_VALUE
    }
}
