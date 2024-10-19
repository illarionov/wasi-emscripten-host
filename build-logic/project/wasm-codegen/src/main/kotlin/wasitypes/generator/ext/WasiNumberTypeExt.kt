/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.wasitypes.generator.ext

import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S16
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S32
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S64
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S8
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U16
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U32
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U64
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U8
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT

internal fun WasiNumberType.getNativeType32BitMultiple(): ClassName = when (this) {
    S64, U64 -> LONG
    S8, S16, S32, U8, U16, U32 -> INT
}

internal fun WasiNumberType.getNativeType(): ClassName = when (this) {
    S8, U8 -> BYTE
    S16, U16 -> SHORT
    S32, U32 -> INT
    S64, U64 -> LONG
}
