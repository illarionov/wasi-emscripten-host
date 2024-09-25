/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.generator.ext.buildWasiPreviewTypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.className
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.UnionType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.KModifier.SEALED
import com.squareup.kotlinpoet.TypeSpec

internal class UnionTypeGenerator(
    private val identifier: String,
    private val typedef: UnionType,
    private val typenameRaw: String,
    private val comment: String,
    private val typenamesPackage: String,
) {
    private val className = identifier.className(typenamesPackage)

    fun generate(): FileSpec = FileSpec.builder(className)
        .addType(generateSealedInterface())
        .build()

    private fun generateSealedInterface(): TypeSpec = TypeSpec.interfaceBuilder(className)
        .addModifiers(PUBLIC, SEALED)
        .addWasiPrevie1UnionTypeKdoc()
        .build()

    private fun TypeSpec.Builder.addWasiPrevie1UnionTypeKdoc(): TypeSpec.Builder {
        val members: List<ClassName> = typedef.members.toSet().map {
            it.className(typenamesPackage)
        }
        val membersTemplate = members.joinToString(", ") { "[%T]" }

        val template = buildWasiPreviewTypeKdoc(identifier, comment, typenameRaw) {
            add("Union tag: [%T]")
            add("Members: $membersTemplate")
        }
        return this.addKdoc(
            format = template,
            args = (listOf(typedef.tag.className(typenamesPackage)) + members).toTypedArray(),
        )
    }
}
