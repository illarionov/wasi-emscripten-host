/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.ext

import at.released.weh.host.base.WasmValueType
import at.released.weh.host.base.function.HostFunction.HostFunctionType
import io.github.charlietap.chasm.ast.type.NumberType
import io.github.charlietap.chasm.ast.type.ResultType
import io.github.charlietap.chasm.ast.type.ValueType
import io.github.charlietap.chasm.ast.type.VectorType.V128
import io.github.charlietap.chasm.ast.type.FunctionType as ChasmFunctionType

internal fun List<HostFunctionType>.toChasmFunctionTypes(): Map<HostFunctionType, ChasmFunctionType> = associateWith(
    HostFunctionType::toChasmFunctionType,
)

internal fun HostFunctionType.toChasmFunctionType(): ChasmFunctionType = ChasmFunctionType(
    ResultType(paramTypes.map(WasmValueType::toChasmValueTypes)),
    ResultType(returnTypes.map(WasmValueType::toChasmValueTypes)),
)

internal fun WasmValueType.toChasmValueTypes(): ValueType = when (this) {
    WasmValueType.I32 -> ValueType.Number(NumberType.I32)
    WasmValueType.I64 -> ValueType.Number(NumberType.I64)
    WasmValueType.F32 -> ValueType.Number(NumberType.F32)
    WasmValueType.F64 -> ValueType.Number(NumberType.F64)
    WasmValueType.V128 -> ValueType.Vector(V128)
    else -> error("Unsupported WASM value type `$this`")
}
