/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.wasitypes.generator.ext

import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber

internal fun formatWasiPreview1TypeKdoc(
    identifier: String,
    comment: String,
    typenameRaw: String,
): String = buildWasiPreviewTypeKdoc(identifier, comment, typenameRaw) {}

internal fun formatWasiPreview1EnumTypeKdoc(
    identifier: String,
    comment: String,
    tag: UnsignedNumber,
    typenameRaw: String,
): String = buildWasiPreviewTypeKdoc(identifier, comment, typenameRaw) {
    add("TAG: `${tag.toString().lowercase()}`")
}

internal fun formatWasiPreview1FlagsTypeKdoc(
    identifier: String,
    comment: String,
    repr: UnsignedNumber,
    typenameRaw: String,
): String = buildWasiPreviewTypeKdoc(identifier, comment, typenameRaw) {
    add("Representation: `${repr.toString().lowercase()}`")
}

internal fun formatWasiPreview1RecordTypeKdoc(
    identifier: String,
    comment: String,
    paramDescription: List<Pair<String, String>>,
    typenameRaw: String,
): String = buildWasiPreviewTypeKdoc(identifier, comment, typenameRaw) {
    val paramsBlock = paramDescription.joinToString("\n") { (paramName, paramDescription) ->
        "@param $paramName $paramDescription"
    }
    add(paramsBlock)
}

fun buildWasiPreviewTypeKdoc(
    identifier: String,
    comment: String,
    typenameRaw: String,
    content: MutableList<String>.() -> Unit,
): String = buildList {
    add(formatIdentifierHeader(identifier))
    if (comment.isNotEmpty()) {
        add(comment)
    }

    content()

    if (typenameRaw.isNotEmpty()) {
        add(formatDefinition(typenameRaw))
    }
}.joinToString("\n\n")

private fun formatIdentifierHeader(identifier: String) = "WASI Preview1 type `$$identifier`"

internal fun formatDefinition(typenameRaw: String) = "Definition:\n```\n$typenameRaw\n```"
