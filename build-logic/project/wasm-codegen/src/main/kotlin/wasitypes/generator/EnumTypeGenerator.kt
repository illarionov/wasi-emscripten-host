/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.wasitypes.generator

import at.released.weh.gradle.wasm.codegen.util.className
import at.released.weh.gradle.wasm.codegen.util.classname.JVM_STATIC_CLASS_NAME
import at.released.weh.gradle.wasm.codegen.util.toUppercaseWithUnderscores
import at.released.weh.gradle.wasm.codegen.wasitypes.generator.ext.formatWasiPreview1EnumTypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.EnumType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Companion.anonymousClassBuilder

internal class EnumTypeGenerator(
    private val identifier: String,
    private val typedef: EnumType,
    private val comment: String,
    private val typenameRaw: String,
    typenamesPackage: String,
) {
    private val className = identifier.className(typenamesPackage)

    fun generate(): FileSpec {
        val builder = FileSpec.builder(className)
        builder.addType(generateEnum())
        return builder.build()
    }

    private fun generateEnum(): TypeSpec {
        val builder = TypeSpec.enumBuilder(className)
            .addKdoc(formatWasiPreview1EnumTypeKdoc(identifier, comment, typedef.tag, typenameRaw))
            .addModifiers(PUBLIC)

        typedef.values.forEach { (identifier, comment) ->
            val enumConstantSpec = anonymousClassBuilder()
                .apply {
                    if (comment.isNotEmpty()) {
                        addKdoc(comment)
                    }
                }
                .build()

            builder.addEnumConstant(identifier.toUppercaseWithUnderscores(), enumConstantSpec)
        }

        builder.addProperty(
            PropertySpec.builder("code", INT, PUBLIC)
                .getter(FunSpec.getterBuilder().addCode("return ordinal").build())
                .build(),
        )
        builder.addType(generateEnumCompanion())

        return builder.build()
    }

    private fun generateEnumCompanion(): TypeSpec {
        val fromCodeBuilder = FunSpec.builder("fromCode")
            .addParameter("code", INT)
            .addAnnotation(JVM_STATIC_CLASS_NAME)
            .returns(className.copy(nullable = true))
            .addCode("return entries.getOrNull(code)")
            .build()

        return TypeSpec.companionObjectBuilder()
            .addFunction(fromCodeBuilder)
            .build()
    }
}
