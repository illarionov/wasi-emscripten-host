/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.ext

import at.released.weh.host.base.WasmValueType
import at.released.weh.host.base.function.HostFunction.HostFunctionType
import io.github.charlietap.chasm.embedding.shapes.ValueType
import io.github.charlietap.chasm.embedding.shapes.FunctionType as ChasmFunctionType

internal fun List<HostFunctionType>.toChasmFunctionTypes(): Map<HostFunctionType, ChasmFunctionType> = associateWith(
    HostFunctionType::toChasmFunctionType,
)

internal fun HostFunctionType.toChasmFunctionType(): ChasmFunctionType = ChasmFunctionType(
    paramTypes.map(WasmValueType::toChasmValueTypes),
    returnTypes.map(WasmValueType::toChasmValueTypes),
)

internal fun WasmValueType.toChasmValueTypes(): ValueType = when (this) {
    WasmValueType.I32 -> ValueType.Number.I32
    WasmValueType.I64 -> ValueType.Number.I64
    WasmValueType.F32 -> ValueType.Number.F32
    WasmValueType.F64 -> ValueType.Number.F64
    else -> error("Unsupported WASM value type `$this`")
}
