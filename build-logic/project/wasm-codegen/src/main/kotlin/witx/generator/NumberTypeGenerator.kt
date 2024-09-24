/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.generator.ext.AndroidAnnotationExt
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.AndroidAnnotationExt.AndroidxAnnotationType.INT_DEF
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.AndroidAnnotationExt.AndroidxAnnotationType.LONG_DEF
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.AndroidAnnotationExt.createAndroidAnnotation
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.className
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.createRetentionAnnotation
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.createTargetAnnotation
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatWasiPrevie1TypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.getNativeType32BitMultiple
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.markerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.annotation.AnnotationRetention.SOURCE

internal class NumberTypeGenerator(
    private val identifier: String,
    private val typedef: WasiNumberType,
    private val comment: String,
    private val typenameRaw: String,
    typenamesPackage: String,
) {
    private val className = identifier.className(typenamesPackage)
    private val annotationClassName = identifier.markerAnnotationClassName(typenamesPackage)

    fun generate(): FileSpec {
        val builder = FileSpec.builder(className)
        builder.addTypeAlias(generateTypealias())
        builder.addType(generateMarkerAnnotation())
        return builder.build()
    }

    private fun generateTypealias(): TypeAliasSpec = TypeAliasSpec.builder(
        className.simpleName,
        typedef.getNativeType32BitMultiple(),
    )
        .addModifiers(PUBLIC)
        .addKdoc(formatWasiPrevie1TypeKdoc(identifier, comment, typenameRaw))
        .build()

    private fun generateMarkerAnnotation(): TypeSpec {
        val androidAnnotationType = when (typedef.getNativeType32BitMultiple()) {
            LONG -> LONG_DEF
            else -> INT_DEF
        }

        val annotations = listOf(
            createRetentionAnnotation(SOURCE),
            createTargetAnnotation(AndroidAnnotationExt.defaultTarget),
            createAndroidAnnotation(androidAnnotationType, false, emptyList()),
        )

        return TypeSpec.annotationBuilder(annotationClassName)
            .addModifiers(PUBLIC)
            .addAnnotations(annotations)
            .addKdoc(
                "Specifies that the annotated element of integer type represents the value of type" +
                        " [${className.simpleName}].",
            )
            .build()
    }
}
