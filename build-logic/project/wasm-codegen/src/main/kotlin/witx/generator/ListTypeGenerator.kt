/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.generator.ext.AndroidAnnotationExt
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.className
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.createRetentionAnnotation
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.createTargetAnnotation
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatWasiPrevie1TypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.markerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.annotation.AnnotationRetention.SOURCE

internal class ListTypeGenerator(
    private val identifier: String,
    private val typedef: ListType,
    private val typenameRaw: String,
    private val comment: String,
    private val typenamesPackage: String,
) {
    private val className = identifier.className(typenamesPackage)
    private val annotationClassName = identifier.markerAnnotationClassName(typenamesPackage)

    fun generate(): FileSpec {
        val builder = FileSpec.builder(className)
        builder.addTypeAlias(generateTypealias())
        builder.addType(generateMarkerAnnotation())
        return builder.build()
    }

    private fun generateTypealias(): TypeAliasSpec {
        val listType = typedef.identifier.className(typenamesPackage)
        return TypeAliasSpec.builder(
            className.simpleName,
            LIST.parameterizedBy(listType),
        )
            .addModifiers(PUBLIC)
            .addKdoc(formatWasiPrevie1TypeKdoc(identifier, comment, typenameRaw))
            .build()
    }

    private fun generateMarkerAnnotation(): TypeSpec {
        val annotations = listOf(
            createRetentionAnnotation(SOURCE),
            createTargetAnnotation(AndroidAnnotationExt.defaultTarget),
        )

        return TypeSpec.annotationBuilder(annotationClassName)
            .addModifiers(PUBLIC)
            .addAnnotations(annotations)
            .addKdoc(
                "Specifies that the annotated element of list type represents the value of type " +
                        "List<[${className.simpleName}]>.",
            )
            .build()
    }
}
