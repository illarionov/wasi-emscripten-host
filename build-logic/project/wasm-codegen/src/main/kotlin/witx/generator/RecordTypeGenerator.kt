/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.generator.ext.className
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.flagsMarkerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatWasiPrevie1RecordTypeKdoc
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.markerAnnotationClassName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.toUpperCamelCaseClassName
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordField
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordFieldType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordFieldType.IdentifierField
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType.RecordFieldType.Pointer
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiTypename
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.DATA
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.apache.tools.ant.taskdefs.Input.HandlerType

internal class RecordTypeGenerator(
    private val identifier: String,
    private val typedef: RecordType,
    private val comment: String,
    private val typenameRaw: String,
    private val typenamesPackage: String,
    private val typenames: Map<String, WasiTypename>,
    implementMembers: Set<Identifier>,
) {
    private val className = ClassName(typenamesPackage, identifier.toUpperCamelCaseClassName())
    private val superInterfaces = implementMembers.map {
        it.className(typenamesPackage)
    }

    fun generate(): FileSpec = FileSpec.builder(className)
        .addType(generateRecordDataClass())
        .build()

    private fun generateRecordDataClass(): TypeSpec {
        val paramsDescription = typedef.fields.map {
            it.identifier.toCamelCasePropertyName() to it.comment
        }
        val constructor = FunSpec.constructorBuilder().apply {
            typedef.fields.forEach { record: RecordField ->
                addParameter(record.identifier.toCamelCasePropertyName(), record.type.getTypeName())
            }
        }.build()
        val properties: List<PropertySpec> = typedef.fields.map { (fieldIdentifier, fieldType, _) ->
            val propertyName = fieldIdentifier.toCamelCasePropertyName()
            val builder = PropertySpec.builder(propertyName, fieldType.getTypeName())
                .initializer(propertyName)

            getPropertyMarkerAnnotationSpec(fieldType)?.let {
                builder.addAnnotation(it)
            }

            if (fieldType is Pointer) {
                builder.addKdoc(fieldType.getPointerDescription())
            }

            builder.build()
        }

        return TypeSpec.classBuilder(className)
            .addModifiers(PUBLIC, DATA)
            .primaryConstructor(constructor)
            .addKdoc(formatWasiPrevie1RecordTypeKdoc(identifier, comment, paramsDescription, typenameRaw))
            .addProperties(properties)
            .addSuperinterfaces(superInterfaces)
            .build()
    }

    private fun RecordFieldType.getTypeName(): TypeName = when (this) {
        is IdentifierField -> this.identifier.className(typenamesPackage)
        is Pointer -> INT
    }

    private fun getPropertyMarkerAnnotationSpec(type: RecordFieldType): AnnotationSpec? = when (type) {
        is IdentifierField -> getMarkerAnnotationSpecForType(type.identifier)
        is Pointer -> null
    }

    private fun getMarkerAnnotationSpecForType(typeIdentifier: String): AnnotationSpec? {
        val typedef: WasiType = typenames[typeIdentifier]?.typedef ?: return null

        return when (typedef) {
            is NumberType, is ListType, is HandlerType -> AnnotationSpec.builder(
                typeIdentifier.markerAnnotationClassName(typenamesPackage),
            ).build()

            is FlagsType -> AnnotationSpec.builder(
                typeIdentifier.flagsMarkerAnnotationClassName(typenamesPackage),
            ).build()

            else -> null
        }
    }

    private fun Pointer.getPointerDescription(): String {
        val pointerType = if (this.isConstPointer) {
            "const_pointer"
        } else {
            "pointer"
        }
        return "($pointerType ${this.dstType.name.lowercase()})"
    }
}
