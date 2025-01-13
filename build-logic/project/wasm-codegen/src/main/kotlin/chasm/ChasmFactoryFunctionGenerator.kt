/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm

import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmBindingsClassname
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmShapesClassname
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmShapesClassname.AstType
import at.released.weh.gradle.wasm.codegen.util.classname.WehHostClassname
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasiPreview1ClassName
import at.released.weh.gradle.wasm.codegen.util.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I32
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I64
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.Companion.wasmType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.ListOfBaseWebAssemblyTypes.listOfTypesComparator
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.ListOfBaseWebAssemblyTypes.listPropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.NamedParamType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING

internal class ChasmFactoryFunctionGenerator(
    wasiTypenames: Map<Identifier, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
    private val functionsClassName: ClassName,
    private val factoryFunctionName: String = "createWasiPreview1HostFunctions",
) {
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypenames)

    fun generate(): FunSpec = FunSpec.builder(factoryFunctionName).apply {
        addModifiers(INTERNAL)
        addParameter("store", ChasmShapesClassname.STORE)
        addParameter("memory", ChasmBindingsClassname.CHASM_MEMORY_ADAPTER)
        addParameter("wasiMemoryReader", WehWasiPreview1ClassName.WASI_MEMORY_READER)
        addParameter("wasiMemoryWriter", WehWasiPreview1ClassName.WASI_MEMORY_WRITER)
        addParameter("host", WehHostClassname.EMBEDDER_HOST)
        addParameter(
            ParameterSpec.builder("moduleName", STRING).defaultValue("%S", "wasi_snapshot_preview1").build(),
        )
        returns(LIST.parameterizedBy(ChasmShapesClassname.IMPORT))

        addCode("val %N = %T(%M)\n", "i", AstType.VALUE_TYPE_NUMBER, AstType.AST_NUMBER_TYPE_I32)
        addCode("val %N = %T(%M)\n", "j", AstType.VALUE_TYPE_NUMBER, AstType.AST_NUMBER_TYPE_I64)

        getResultTypes().forEach { listOfArgs: List<BaseWebAssemblyType> ->
            val propertyName = listOfArgs.listPropertyName()
            val args: List<Any> = buildList {
                add(propertyName)
                add(AstType.AST_RESULT_TYPE)
                listOfArgs.map {
                    when (it) {
                        I32 -> "i"
                        I64 -> "j"
                    }
                }.let(::addAll)
            }
            addCode(
                format = listOfArgs.joinToString(
                    prefix = "val %N = %T(listOf(",
                    postfix = "))\n",
                    separator = ",",
                ) { "%N" },
                args = args.toTypedArray(),
            )
        }

        getFunctionTypeInstances().forEach { functionType ->
            addCode(
                "val %N = %T(%N, %N)\n",
                functionType.propertyName,
                AstType.AST_FUNCTION_TYPE,
                functionType.input.listPropertyName(),
                functionType.results.listPropertyName(),
            )
        }

        addCode("val functions = %T(host, memory, wasiMemoryReader, wasiMemoryWriter)\n", functionsClassName)

        addCode("return listOf(⇥⇥\n")
        wasiFunctions.forEach { wasiFunc: WasiFunc ->
            val baseType = BaseFunctionType.fromWasiFunc(wasiFunc, baseTypeResolver)
            val wasiNameCamelCase = wasiFunc.export.toCamelCasePropertyName()

            addCode(
                "%T(moduleName, %S, %M(store, %N, functions::%N)),\n",
                ChasmShapesClassname.IMPORT,
                wasiFunc.export,
                ChasmShapesClassname.CHASM_EMBEDDING_FUNCTION,
                baseType.propertyName,
                wasiNameCamelCase,
            )
        }
        addCode("⇤⇤)")
    }.build()

    private fun getResultTypes(): Set<List<BaseWebAssemblyType>> {
        return wasiFunctions.flatMap { wasiFunc ->
            listOf(
                baseTypeResolver.getFuncInputArgs(wasiFunc),
                baseTypeResolver.getFuncReturnTypes(wasiFunc),
            )
        }
            .map { args: List<NamedParamType> -> args.map { it.baseType.wasmType } }
            .toSortedSet(listOfTypesComparator)
    }

    private fun getFunctionTypeInstances(): Set<BaseFunctionType> {
        return wasiFunctions.map { BaseFunctionType.fromWasiFunc(it, baseTypeResolver) }.toSortedSet()
    }
}
