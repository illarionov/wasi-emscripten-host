/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator

import at.released.weh.gradle.wasm.codegen.util.classname.JVM_STATIC_CLASS_NAME
import at.released.weh.gradle.wasm.codegen.util.classname.WasiValueTypesMemberName
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasmCoreClassName
import at.released.weh.gradle.wasm.codegen.util.toUppercaseWithUnderscores
import at.released.weh.gradle.wasm.codegen.witx.generator.ext.formatDefinition
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.NamedParamType
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.HANDLE
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S16
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S8
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData.Tuple
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
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
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypes)
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
            .addSuperinterface(WehWasmCoreClassName.HOST_FUNCTION)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("wasmName", STRING)
                    .addParameter("type", WehWasmCoreClassName.HOST_FUNCTION_TYPE)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("wasmName", STRING, PUBLIC, OVERRIDE)
                    .initializer("wasmName")
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("type", WehWasmCoreClassName.HOST_FUNCTION_TYPE, PUBLIC, OVERRIDE)
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
        WehWasmCoreClassName.HOST_FUNCTION_TYPE,
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
        val paramTypes: List<NamedParamType> = buildList {
            addAll(
                params.flatMap(baseTypeResolver::getFuncParamBaseTypes),
            )
            result?.expectedData?.let { expectedData: ExpectedData ->
                when (expectedData) {
                    is ExpectedData.WasiType -> {
                        val expectedPointer = NamedParamType(
                            baseType = WasiBaseWasmType.POINTER,
                            comment = "expected ${expectedData.identifier}",
                        )
                        add(expectedPointer)
                    }

                    is Tuple -> {
                        val expectedFirstPointer = NamedParamType(
                            baseType = WasiBaseWasmType.POINTER,
                            comment = "expected ${expectedData.first}",
                        )
                        add(expectedFirstPointer)

                        val expectedSecondPointer = NamedParamType(
                            baseType = WasiBaseWasmType.POINTER,
                            comment = "expected ${expectedData.second}",
                        )
                        add(expectedSecondPointer)
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
                .flatMap { listOf(it.comment, it.baseType.valueTypeMemberName) }
                .toTypedArray(),
        )
    }

    private fun generateReturnTypeSpec(
        result: WasiFuncResult?,
    ): CodeBlock {
        if (result == null) {
            return CodeBlock.of("null\n")
        }
        val errorArgument = baseTypeResolver.getWasiBaseType(
            identifier = result.expectedError,
            parameterComment = result.expectedError,
        ).toList().single()

        return CodeBlock.of("/* %L */ %M\n", errorArgument.comment, errorArgument.baseType.valueTypeMemberName)
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

    private companion object {
        private val WasiBaseWasmType.valueTypeMemberName: MemberName
            get() = when (this) {
                WasiBaseWasmType.POINTER -> WasiValueTypesMemberName.POINTER
                WasiBaseWasmType.U8 -> WasiValueTypesMemberName.U8
                WasiBaseWasmType.U16 -> WasiValueTypesMemberName.U16
                WasiBaseWasmType.S32 -> WasiValueTypesMemberName.S32
                WasiBaseWasmType.U32 -> WasiValueTypesMemberName.U32
                WasiBaseWasmType.S64 -> WasiValueTypesMemberName.S64
                WasiBaseWasmType.U64 -> WasiValueTypesMemberName.U64
                HANDLE -> WasiValueTypesMemberName.HANDLE
                S8, S16 -> error("Unsupported type $this")
            }
    }
}
