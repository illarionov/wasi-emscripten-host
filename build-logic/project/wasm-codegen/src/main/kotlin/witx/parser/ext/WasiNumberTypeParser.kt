/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser.ext

import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber
import org.antlr.v4.runtime.tree.TerminalNode

internal fun parseNumberType(node: TerminalNode): WasiNumberType = when (node.text) {
    "s8" -> WasiNumberType.SignedNumber.S8
    "s16" -> WasiNumberType.SignedNumber.S16
    "s32" -> WasiNumberType.SignedNumber.S32
    "s64" -> WasiNumberType.SignedNumber.S64
    else -> parseUnsignedNumberType(node)
}

internal fun parseUnsignedNumberType(node: TerminalNode): UnsignedNumber = when (node.text) {
    "u8" -> UnsignedNumber.U8
    "u16" -> UnsignedNumber.U16
    "u32" -> UnsignedNumber.U32
    "u64" -> UnsignedNumber.U64
    else -> error("Unknown type ${node.text}")
}
