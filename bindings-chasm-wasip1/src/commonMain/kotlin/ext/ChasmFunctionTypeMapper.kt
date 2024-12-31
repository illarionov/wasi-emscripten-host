/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.HostFunction.HostFunctionType
import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes
import io.github.charlietap.chasm.embedding.shapes.ValueType
import io.github.charlietap.chasm.embedding.shapes.FunctionType as ChasmFunctionType

@InternalWasiEmscriptenHostApi
public fun List<HostFunctionType>.toChasmFunctionTypes(): Map<HostFunctionType, ChasmFunctionType> = associateWith(
    HostFunctionType::toChasmFunctionType,
)

internal fun HostFunctionType.toChasmFunctionType(): ChasmFunctionType = ChasmFunctionType(
    paramTypes.map(::wasmValueTypeToChasmValueTypes),
    returnTypes.map(::wasmValueTypeToChasmValueTypes),
)

internal fun wasmValueTypeToChasmValueTypes(
    @WasmValueType type: Int,
): ValueType = when (type) {
    WasmValueTypes.I32 -> ValueType.Number.I32
    WasmValueTypes.I64 -> ValueType.Number.I64
    WasmValueTypes.F32 -> ValueType.Number.F32
    WasmValueTypes.F64 -> ValueType.Number.F64
    else -> error("Unsupported WASM value type `$type`")
}
