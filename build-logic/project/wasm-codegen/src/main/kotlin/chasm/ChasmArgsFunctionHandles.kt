/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm

import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmBindingsClassname
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmShapesClassname
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmShapesClassname.HOST_FUNCTION_CONTEXT
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
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class ChasmArgsFunctionHandles(
    wasiTypes: Map<Identifier, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
) {
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypes)

    fun getFunctionHandles(): List<WasiFunctionHandle> {
        return wasiFunctions.map { wasiFunc: WasiFunc ->
            WasiFunctionHandle(
                func = wasiFunc,
                chasmHostFunctionName = wasiFunc.export.toCamelCasePropertyName(),
            )
        }.sortedBy { it.func.export }
    }

    inner class WasiFunctionHandle(
        val func: WasiFunc,
        val chasmHostFunctionName: String,
    ) {
        val handleProperty = WasiFunctionHandlerProperty(func)

        fun chasmHostFunctionDeclaration(): FunSpec = FunSpec.builder(chasmHostFunctionName).apply {
            addParameter("context", HOST_FUNCTION_CONTEXT)
            addParameter("args", LIST.parameterizedBy(ChasmShapesClassname.VALUE))
            returns(LIST.parameterizedBy(ChasmShapesClassname.VALUE))

            val handleArgs: List<Pair<String, MemberName>> =
                baseTypeResolver.getFuncInputArgs(func).mapIndexed { index, (baseType: WasiBaseWasmType, comment) ->
                    val converterFunc = when (baseType) {
                        POINTER -> ChasmBindingsClassname.ChasmExt.VALUE_AS_WASM_ADDR
                        S8, U8 -> ChasmBindingsClassname.ChasmExt.VALUE_AS_BYTE
                        S16, U16 -> ChasmBindingsClassname.ChasmExt.VALUE_AS_SHORT
                        S32, U32, HANDLE -> ChasmBindingsClassname.ChasmExt.VALUE_AS_INT
                        S64, U64 -> ChasmBindingsClassname.ChasmExt.VALUE_AS_LONG
                    }
                    "\nargs[$index].%M(), /* $comment */" to converterFunc
                }

            val allArgs: List<Pair<String, Any>> = buildList {
                if (func.export !in NO_MEMORY_FUNCTIONS) {
                    add("\n%N," to "memory")
                }
                if (func.export in WASI_MEMORY_READER_FUNCTIONS) {
                    add("\n%N," to "wasiMemoryReader")
                }
                if (func.export in WASI_MEMORY_WRITER_FUNCTIONS) {
                    add("\n%N," to "wasiMemoryWriter")
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
                args = (listOf(handleProperty.propertyName) + allArgs.map(Pair<String, Any>::second)).toTypedArray(),
            )
        }.build()
    }
}
