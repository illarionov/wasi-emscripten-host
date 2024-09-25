/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.witx.generator.ext.JVM_STATIC_CLASS_NAME
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.WasiValueTypesMemberName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.WasiValueTypesMemberName.POINTER
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.WasmCoreClassName
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatDefinition
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.toUppercaseWithUnderscores
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType.Pointer
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData.Tuple
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S32
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.SignedNumber.S64
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U16
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U32
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U64
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber.U8
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.EnumType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.Handle
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.UnionType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

internal class WasiFunctionsGenerator(
    private val functions: List<WasiFunc>,
    wasiTypes: Map<Identifier, WasiType>,
    private val outputDirectory: File,
    functionsPackage: String,
    functionsClassName: String = "WasiPreview1HostFunction",
) {
    private val wasiTypes = WasiTypes(wasiTypes)
    private val className = ClassName(functionsPackage, functionsClassName)

    fun generate() {
        val spec = FileSpec.builder(className)
            .addType(generateFunctionsEnum())
            .build()
        spec.writeTo(outputDirectory)
    }

    private fun generateFunctionsEnum(): TypeSpec {
        val builder = TypeSpec.enumBuilder(className)
            .addKdoc(
                """
                WASI Preview1 function descriptors

                https://github.com/WebAssembly/WASI/blob/main/legacy/preview1/witx/wasi_snapshot_preview1.witx
                """.trimIndent(),
            )
            .addSuperinterface(WasmCoreClassName.HOST_FUNCTION)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("wasmName", STRING)
                    .addParameter("type", WasmCoreClassName.HOST_FUNCTION_TYPE)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("wasmName", STRING, PUBLIC, OVERRIDE)
                    .initializer("wasmName")
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("type", WasmCoreClassName.HOST_FUNCTION_TYPE, PUBLIC, OVERRIDE)
                    .initializer("type")
                    .build(),
            )
            .addFunction(
                FunSpec.constructorBuilder()
                    .addModifiers(PRIVATE)
                    .addParameter("wasmName", String::class)
                    .addParameter("paramTypes", LIST.parameterizedBy(INT))
                    .addParameter("returnType", INT.copy(nullable = true))
                    .callThisConstructor(generateSecondaryEnumConstructor())
                    .build(),
            )
            .addType(generateEnumCompanion())

        functions.forEach { function: WasiFunc ->
            builder.addEnumConstant(
                function.export.toUppercaseWithUnderscores(),
                generateFunctionTypeSpec(function),
            )
        }

        return builder.build()
    }

    private fun generateSecondaryEnumConstructor() = CodeBlock.of(
        """⇥⇥
        |wasmName = wasmName,
        |type = %T(
        |    params = paramTypes,
        |    returnTypes = if (returnType != null) {
        |        listOf(returnType)
        |    } else {
        |        emptyList()
        |    },
        |),⇤⇤
        |
        """.trimMargin(),
        WasmCoreClassName.HOST_FUNCTION_TYPE,
    )

    private fun generateEnumCompanion(): TypeSpec {
        val fromCodeBuilder = PropertySpec.builder("byWasmName", MAP.parameterizedBy(STRING, className), PUBLIC)
            .addAnnotation(JVM_STATIC_CLASS_NAME)
            .initializer("""entries.associateBy(%T::wasmName)""", className)
            .build()

        return TypeSpec.companionObjectBuilder()
            .addProperty(fromCodeBuilder)
            .build()
    }

    private fun generateFunctionTypeSpec(
        function: WasiFunc,
    ): TypeSpec = TypeSpec.anonymousClassBuilder()
        .addSuperclassConstructorParameter("⇥\nwasmName = %S", function.export)
        .addSuperclassConstructorParameter(
            "\nparamTypes = %L",
            generateParamTypesSpec(function.params, function.result),
        )
        .addSuperclassConstructorParameter("\nreturnType = %L⇤", generateReturnTypeSpec(function.result))
        .addFunctionKdoc(function)
        .build()

    private fun generateParamTypesSpec(
        params: List<WasiFuncParam>,
        result: WasiFuncResult?,
    ): CodeBlock {
        if (params.isEmpty() && result == null) {
            return CodeBlock.of("emptyList()")
        }
        val paramTypes: List<Pair<MemberName, String>> = buildList {
            addAll(
                params.map { funcParam ->
                    wasiTypes.getTypeRef(funcParam) to funcParam.name
                },
            )
            result?.expectedData?.let { expectedData: ExpectedData ->
                when (expectedData) {
                    is ExpectedData.WasiType ->
                        add(POINTER to "expected ${expectedData.identifier}")

                    is Tuple -> {
                        add(POINTER to "expected ${expectedData.first}")
                        add(POINTER to "expected ${expectedData.second}")
                    }
                }
            }
        }
        val template = paramTypes.joinToString(prefix = "listOf(⇥\n", postfix = "⇤)", separator = "") {
            "/* %L */ %M,\n"
        }
        return CodeBlock.of(
            format = template,
            args = paramTypes
                .flatMap { listOf(it.second, it.first) }
                .toTypedArray(),
        )
    }

    private fun generateReturnTypeSpec(
        result: WasiFuncResult?,
    ): CodeBlock {
        if (result == null) {
            return CodeBlock.of("null")
        }
        return CodeBlock.of(
            "/* %L */ %M\n",
            result.expectedError,
            wasiTypes.getTypeRef(result.expectedError),
        )
    }

    private fun TypeSpec.Builder.addFunctionKdoc(
        function: WasiFunc,
    ): TypeSpec.Builder {
        val template = listOf(
            "WASI Preview1 function `${function.export}`",
            formatDefinition(function.source),
        ).filter { it.isNotEmpty() }
            .joinToString("\n\n")
        return this.addKdoc(template)
    }

    private class WasiTypes(
        private val wasiTypes: Map<Identifier, WasiType>,
    ) {
        fun getTypeRef(
            param: WasiFuncParam,
        ): MemberName = when (val paramType = param.type) {
            is ParamType.NumberType -> getNumberTypeRef(paramType.type)
            is Pointer -> POINTER
            ParamType.String -> POINTER
            is ParamType.WasiType -> getTypeRef(paramType.identifier)
        }

        fun getTypeRef(
            identifier: String,
        ): MemberName {
            val type: WasiType = wasiTypes[identifier] ?: error("Unknown type $identifier")
            return when (type) {
                is NumberType -> getNumberTypeRef(type.type)
                is EnumType -> getNumberTypeRef(type.tag)
                is FlagsType -> getNumberTypeRef(type.repr)
                Handle -> WasiValueTypesMemberName.HANDLE
                is ListType -> getTypeRef(type.identifier) // XXX check
                is RecordType -> POINTER // XXX check
                is UnionType -> getTypeRef(type.tag)
            }
        }

        private fun getNumberTypeRef(
            numberType: WasiNumberType,
        ): MemberName = when (numberType) {
            S32 -> WasiValueTypesMemberName.S32
            S64 -> WasiValueTypesMemberName.S64
            U8 -> WasiValueTypesMemberName.U8
            U16 -> WasiValueTypesMemberName.U16
            U32 -> WasiValueTypesMemberName.U32
            U64 -> WasiValueTypesMemberName.U64
            else -> error("Unsupported type $numberType")
        }
    }
}
