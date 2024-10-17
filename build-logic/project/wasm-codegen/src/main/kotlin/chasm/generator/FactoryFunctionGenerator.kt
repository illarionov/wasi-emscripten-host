/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm.generator

import at.released.weh.gradle.wasm.codegen.chasm.generator.classname.ChasmBindingsClassname
import at.released.weh.gradle.wasm.codegen.chasm.generator.classname.ChasmShapesClassname
import at.released.weh.gradle.wasm.codegen.util.classname.WehHostClassname
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasiPreview1ClassName
import at.released.weh.gradle.wasm.codegen.util.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I32
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I64
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.Companion.baseType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.Companion.listOfTypesComparator
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.Companion.listPropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.NamedParamType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING

internal class FactoryFunctionGenerator(
    wasiTypenames: Map<Identifier, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
    private val functionsClassName: ClassName,
    private val factoryFunctionName: String = "createWasiPreview1HostFunctionsNew",
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

        getListInstances().forEach { listOfArgs: List<BaseWebAssemblyType> ->
            val propertyName = listOfArgs.listPropertyName()
            addCode(
                format = listOfArgs.joinToString(
                    prefix = "val %N: List<%T> = listOf(",
                    postfix = ")\n",
                    separator = ", ",
                ) { "%M" },
                args = (
                        listOf(propertyName, ChasmShapesClassname.VALUE_TYPE) +
                                listOfArgs.map { it.chasmValueType }
                        ).toTypedArray(),
            )
        }

        getFunctionTypeInstances().forEach { functionType ->
            addCode(
                "val %N = %T(%N, %N)\n",
                functionType.propertyName,
                ChasmShapesClassname.FUNCTION_TYPE,
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

    private fun getListInstances(): Set<List<BaseWebAssemblyType>> {
        return wasiFunctions.flatMap { wasiFunc ->
            listOf(
                baseTypeResolver.getFuncInputArgs(wasiFunc),
                baseTypeResolver.getFuncReturnTypes(wasiFunc),
            )
        }
            .map { args: List<NamedParamType> -> args.map { it.baseType.baseType } }
            .toSortedSet(listOfTypesComparator)
    }

    private fun getFunctionTypeInstances(): Set<BaseFunctionType> {
        return wasiFunctions.map { BaseFunctionType.fromWasiFunc(it, baseTypeResolver) }.toSortedSet()
    }

    private companion object {
        private val BaseWebAssemblyType.chasmValueType: MemberName
            get() = when (this) {
                I32 -> ChasmShapesClassname.VALUE_TYPE_I32
                I64 -> ChasmShapesClassname.VALUE_TYPE_I64
            }
    }
}
