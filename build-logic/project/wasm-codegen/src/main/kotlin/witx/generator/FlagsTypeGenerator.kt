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
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.flagsMarkerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.flagsObjectName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatWasiPrevie1FlagsTypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.getNativeType
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.toUppercaseWithUnderscores
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.CONST
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlin.annotation.AnnotationRetention.SOURCE

internal class FlagsTypeGenerator(
    private val identifier: String,
    private val typedef: FlagsType,
    private val comment: String,
    private val typenameRaw: String,
    typenamesPackage: String,
) {
    private val className = identifier.className(typenamesPackage)
    private val flagsObjectName = identifier.flagsObjectName(typenamesPackage)
    private val annotationClassName = identifier.flagsMarkerAnnotationClassName(typenamesPackage)

    fun generate(): FileSpec {
        val builder = FileSpec.builder(className)
        builder.addTypeAlias(generateTypealias())
        builder.addType(generateFlagsObject())
        builder.addType(generateFlagsAnnotation())
        return builder.build()
    }

    private fun generateTypealias(): TypeAliasSpec = TypeAliasSpec.builder(
        className.simpleName,
        typedef.repr.getNativeType(),
    )
        .addModifiers(PUBLIC)
        .addKdoc(formatWasiPrevie1FlagsTypeKdoc(identifier, comment, typedef.repr, typenameRaw))
        .build()

    private fun generateFlagsObject(): TypeSpec {
        val flagNativeType = typedef.repr.getNativeType()
        val properties = typedef.flags.mapIndexed { index, (identifier, comment) ->
            val mask = 1.shl(index)
            PropertySpec.builder(identifier.toUppercaseWithUnderscores(), flagNativeType, PUBLIC, CONST)
                .initializer(
                    "0x%L%L",
                    mask.toString(16).padStart(2, '0'),
                    flagNativeType.toTypePostfix(),
                )
                .apply {
                    if (comment.isNotEmpty()) {
                        addKdoc(comment)
                    }
                }
                .build()
        }
        return TypeSpec
            .objectBuilder(flagsObjectName)
            .addProperties(properties)
            .build()
    }

    private fun TypeName.toTypePostfix(): String = when (this) {
        SHORT -> ".toShort()"
        LONG -> "L"
        else -> ""
    }

    private fun generateFlagsAnnotation(): TypeSpec {
        val nativeType: ClassName = typedef.repr.getNativeType()

        val builder = TypeSpec.annotationBuilder(annotationClassName)
            .addKdoc(
                "Specifies that the annotated element of [${nativeType.simpleName}] type represents the value of type" +
                        " [${className.simpleName}].",
            )
            .addModifiers(PUBLIC)
            .addAnnotation(createRetentionAnnotation(SOURCE))
            .addAnnotation(createTargetAnnotation(AndroidAnnotationExt.defaultTarget))

        val allowedConstants: List<MemberName> = typedef.flags.map { (identifier, _) ->
            MemberName(flagsObjectName, identifier.toUppercaseWithUnderscores())
        }

        val androidAnnotation = when (nativeType) {
            BYTE, SHORT -> null
            INT -> createAndroidAnnotation(INT_DEF, true, allowedConstants)
            LONG -> createAndroidAnnotation(LONG_DEF, true, allowedConstants)
            else -> error("Unknown type")
        }
        androidAnnotation?.let {
            builder.addAnnotation(it)
        }

        return builder.build()
    }
}
