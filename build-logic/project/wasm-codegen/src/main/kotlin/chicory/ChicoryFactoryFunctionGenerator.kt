/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chicory

import at.released.weh.gradle.wasm.codegen.chicory.ChicoryClassname.Bindings.WASI_MEMORY_READER_PROVIDER
import at.released.weh.gradle.wasm.codegen.chicory.ChicoryClassname.Bindings.WASI_MEMORY_WRITER_PROVIDER
import at.released.weh.gradle.wasm.codegen.util.classname.WehHostClassname
import at.released.weh.gradle.wasm.codegen.util.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I32
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I64
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.ListOfBaseWebAssemblyTypes.getListInstances
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.ListOfBaseWebAssemblyTypes.listPropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
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

internal class ChicoryFactoryFunctionGenerator(
    wasiTypenames: Map<Identifier, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
    private val functionsClassName: ClassName,
    private val factoryFunctionName: String = "createHostFunctions",
) {
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypenames)

    fun generate(): FunSpec = FunSpec.builder(factoryFunctionName).apply {
        addModifiers(INTERNAL)
        addParameter("host", WehHostClassname.EMBEDDER_HOST)
        addParameter("memoryProvider", ChicoryClassname.Bindings.CHICORY_MEMORY_PROVIDER_CLASS_NAME)
        addParameter("wasiMemoryReaderProvider", WASI_MEMORY_READER_PROVIDER)
        addParameter("wasiMemoryWriterProvider", WASI_MEMORY_WRITER_PROVIDER)
        addParameter(
            ParameterSpec.builder("moduleName", STRING).defaultValue("%S", "wasi_snapshot_preview1").build(),
        )
        returns(LIST.parameterizedBy(ChicoryClassname.HOST_FUNCTION))

        getListInstances(wasiFunctions, baseTypeResolver).forEach { listOfArgs: List<BaseWebAssemblyType> ->
            val propertyName = listOfArgs.listPropertyName()
            addCode(
                format = listOfArgs.joinToString(
                    prefix = "val %N: List<%T> = listOf(",
                    postfix = ")\n",
                    separator = ", ",
                ) { "%M" },
                args = (
                        listOf(propertyName, ChicoryClassname.VALUE_TYPE) +
                                listOfArgs.map { it.chicoryValueType }
                        ).toTypedArray(),
            )
        }

        addCode(
            "val functions = %T(host, memoryProvider, wasiMemoryReaderProvider, wasiMemoryWriterProvider)\n",
            functionsClassName,
        )

        addCode("return listOf(⇥⇥\n")
        wasiFunctions.forEach { wasiFunc: WasiFunc ->
            val baseType = BaseFunctionType.fromWasiFunc(wasiFunc, baseTypeResolver)
            val wasiNameCamelCase = wasiFunc.export.toCamelCasePropertyName()

            addCode(
                "%T(moduleName, %S, %N, %N, functions::%N),\n",
                ChicoryClassname.HOST_FUNCTION,
                wasiFunc.export,
                baseType.input.listPropertyName(),
                baseType.results.listPropertyName(),
                wasiNameCamelCase,
            )
        }
        addCode("⇤⇤)")
    }.build()

    private companion object {
        private val BaseWebAssemblyType.chicoryValueType: MemberName
            get() = when (this) {
                I32 -> ChicoryClassname.VALUE_TYPE_I32
                I64 -> ChicoryClassname.VALUE_TYPE_I64
            }
    }
}
