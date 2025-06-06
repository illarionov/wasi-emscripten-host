/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.wasitypes.generator

import at.released.weh.gradle.wasm.codegen.util.className
import at.released.weh.gradle.wasm.codegen.util.classname.TypedefAnnotationExt
import at.released.weh.gradle.wasm.codegen.util.classname.TypedefAnnotationExt.TypedefAnnotationType.INT_DEF
import at.released.weh.gradle.wasm.codegen.util.classname.TypedefAnnotationExt.TypedefAnnotationType.LONG_DEF
import at.released.weh.gradle.wasm.codegen.util.classname.TypedefAnnotationExt.createTypedefAnnotation
import at.released.weh.gradle.wasm.codegen.util.classname.createRetentionAnnotation
import at.released.weh.gradle.wasm.codegen.util.classname.createTargetAnnotation
import at.released.weh.gradle.wasm.codegen.util.flagsMarkerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.util.flagsObjectName
import at.released.weh.gradle.wasm.codegen.util.toUppercaseWithUnderscores
import at.released.weh.gradle.wasm.codegen.wasitypes.generator.ext.formatWasiPreview1FlagsTypeKdoc
import at.released.weh.gradle.wasm.codegen.wasitypes.generator.ext.getNativeType
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
        .addKdoc(formatWasiPreview1FlagsTypeKdoc(identifier, comment, typedef.repr, typenameRaw))
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
            .addAnnotation(createTargetAnnotation(TypedefAnnotationExt.defaultTarget))

        val allowedConstants: List<MemberName> = typedef.flags.map { (identifier, _) ->
            MemberName(flagsObjectName, identifier.toUppercaseWithUnderscores())
        }

        val androidAnnotation = when (nativeType) {
            BYTE, SHORT -> null
            INT -> createTypedefAnnotation(INT_DEF, true, allowedConstants)
            LONG -> createTypedefAnnotation(LONG_DEF, true, allowedConstants)
            else -> error("Unknown type")
        }
        androidAnnotation?.let {
            builder.addAnnotation(it)
        }

        return builder.build()
    }
}
