/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("VARIABLE_NAME_INCORRECT")

package at.released.weh.wasm.core

import androidx.annotation.IntDef

/**
 * Binary opcode of the WebAssembly value type.
 *
 * The main opcodes are defined in [WasmValueTypes].
 */
@IntDef(
    value = [
        WasmValueTypes.I32,
        WasmValueTypes.I64,
        WasmValueTypes.F32,
        WasmValueTypes.F64,
        WasmValueTypes.V128,
        WasmValueTypes.FUNC_REF,
        WasmValueTypes.EXTERN_REF,
    ],
)
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
public annotation class WasmValueType
