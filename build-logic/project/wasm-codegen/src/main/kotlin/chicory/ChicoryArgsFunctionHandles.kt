/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

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
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.VARARG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

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
            addParameter("args", ChicoryClassname.VALUE, VARARG)
            returns(ARRAY.parameterizedBy(ChicoryClassname.VALUE))

            val handleArgs: List<Pair<String, MemberName>> =
                baseTypeResolver.getFuncInputArgs(func).mapIndexed { index, (baseType: WasiBaseWasmType, _, comment) ->
                    val (funcSpecifier, converterFunc) = when (baseType) {
                        POINTER -> "%M" to ChicoryClassname.Bindings.VALUE_AS_WASM_ADDR
                        S8, U8 -> "%N" to ChicoryClassname.VALUE_AS_BYTE
                        S16, U16 -> "%N" to ChicoryClassname.VALUE_AS_SHORT
                        S32, U32, HANDLE -> "%N" to ChicoryClassname.VALUE_AS_INT
                        S64, U64 -> "%N" to ChicoryClassname.VALUE_AS_LONG
                    }
                    "\nargs[$index].$funcSpecifier(), /* $comment */" to converterFunc
                }

            val allArgs: List<Pair<String, Any>> = buildList {
                if (func.export !in NO_MEMORY_FUNCTIONS) {
                    add("\nmemoryProvider.get(%N)," to "instance")
                }
                if (func.export in WASI_MEMORY_READER_FUNCTIONS) {
                    add("\n%N(instance)," to "wasiMemoryReaderProvider")
                }
                if (func.export in WASI_MEMORY_WRITER_FUNCTIONS) {
                    add("\n%N(instance)," to "wasiMemoryWriterProvider")
                }

                addAll(handleArgs)
            }

            val toListOfReturnValues = if (func.result != null) {
                ".toListOfReturnValues()"
            } else {
                ""
            }

            addCode(
                format = allArgs.joinToString(
                    separator = "",
                    prefix = "return %N.execute(⇥⇥⇥",
                    postfix = "\n⇤)$toListOfReturnValues⇤⇤\n",
                    transform = Pair<String, *>::first,
                ),
                args = (listOf(handlerProperty.propertyName) + allArgs.map(Pair<String, Any>::second)).toTypedArray(),
            )
        }.build()
    }
}
