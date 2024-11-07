/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.gradle.wasm.codegen.chicory

import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlerProperty
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.NO_MEMORY_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.WASI_MEMORY_READER_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.WASI_MEMORY_WRITER_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.HANDLE
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.POINTER
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S16
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S32
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S64
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S8
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U16
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U32
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U64
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U8
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.VARARG
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY

internal class ChicoryArgsFunctionHandles(
    wasiTypes: Map<Identifier, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
) {
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypes)
    fun getFunctionHandles(): List<ChicoryWasiFunctionHandle> {
        return wasiFunctions.map { wasiFunc: WasiFunc ->
            val wasiNameCamelCase = wasiFunc.export.toCamelCasePropertyName()
            ChicoryWasiFunctionHandle(
                func = wasiFunc,
                hostFunctionName = wasiNameCamelCase,
            )
        }.sortedBy { it.func.export }
    }

    inner class ChicoryWasiFunctionHandle(
        val func: WasiFunc,
        val hostFunctionName: String,
    ) {
        val handlerProperty = WasiFunctionHandlerProperty(func)

        fun hostFunctionDeclaration(): FunSpec = FunSpec.builder(hostFunctionName).apply {
            addParameter("instance", ChicoryClassname.INSTANCE)
            addParameter("args", LONG, VARARG)
            returns(LONG_ARRAY)

            val argsFormatSpecs: MutableList<String> = mutableListOf()
            val argSpecs: MutableList<Any> = mutableListOf()

            argSpecs.add(handlerProperty.propertyName)

            if (func.export !in NO_MEMORY_FUNCTIONS) {
                argsFormatSpecs.add("\nmemoryProvider.get(instance),")
            }
            if (func.export in WASI_MEMORY_READER_FUNCTIONS) {
                argsFormatSpecs.add("\nwasiMemoryReaderProvider(instance),")
            }
            if (func.export in WASI_MEMORY_WRITER_FUNCTIONS) {
                argsFormatSpecs.add("\nwasiMemoryWriterProvider(instance),")
            }
            baseTypeResolver.getFuncInputArgs(func).forEachIndexed { index, (baseType: WasiBaseWasmType, _, comment) ->
                when (baseType) {
                    POINTER -> {
                        argsFormatSpecs.add("\nargs[$index].%M(), /* $comment */")
                        argSpecs.add(ChicoryClassname.Bindings.VALUE_AS_WASM_ADDR)
                    }

                    S8, U8 -> argsFormatSpecs.add("\nargs[$index].toByte(), /* $comment */")
                    S16, U16 -> argsFormatSpecs.add("\nargs[$index].toShort(), /* $comment */")
                    S32, U32, HANDLE -> argsFormatSpecs.add("\nargs[$index].toInt(), /* $comment */")
                    S64, U64 -> argsFormatSpecs.add("\nargs[$index], /* $comment */")
                }
            }

            val toListOfReturnValues = if (func.result != null) {
                ".toListOfReturnValues()"
            } else {
                ""
            }

            addCode(
                format = argsFormatSpecs.joinToString(
                    separator = "",
                    prefix = "return %N.execute(⇥⇥⇥",
                    postfix = "\n⇤)$toListOfReturnValues⇤⇤\n",
                ),
                args = argSpecs.toTypedArray(),
            )
        }.build()
    }
}
