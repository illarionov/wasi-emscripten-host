/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.helper

import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.HANDLE
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.POINTER
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U32
import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncParam.ParamType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc.WasiFuncResult.ExpectedData.Tuple
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.EnumType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.FlagsType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.Handle
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.ListType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.NumberType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.RecordType
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType.UnionType

internal class WasiBaseTypeResolver(
    private val wasiTypes: Map<Identifier, WasiType>,
) {
    fun getFuncParamBaseTypes(
        param: WasiFuncParam,
    ): List<NamedParamType> = when (val paramType = param.type) {
        is ParamType.NumberType -> {
            val number = NamedParamType(
                baseType = getNumberTypeRef(paramType.type),
                comment = param.name,
            )
            listOf(number)
        }

        is ParamType.Pointer -> {
            val pointer = NamedParamType(
                baseType = WasiBaseWasmType.POINTER,
                comment = param.name,
            )
            listOf(pointer)
        }

        ParamType.String -> {
            val stringPointer = NamedParamType(
                baseType = POINTER,
                comment = param.name,
            )
            val stringSize = NamedParamType(
                baseType = U32,
                comment = "${param.name} size",
            )
            listOf(stringPointer, stringSize)
        }

        is ParamType.WasiType -> getWasiBaseType(paramType.identifier, param.name)
    }

    fun getWasiBaseType(
        identifier: Identifier,
        parameterComment: String,
    ): List<NamedParamType> {
        val wasiType: WasiType = wasiTypes[identifier] ?: error("Unknown type $identifier")
        return when (wasiType) {
            is NumberType -> {
                val number = NamedParamType(getNumberTypeRef(wasiType.type), parameterComment)
                listOf(number)
            }

            is EnumType -> {
                val enumRepresentation = NamedParamType(getNumberTypeRef(wasiType.tag), parameterComment)
                listOf(enumRepresentation)
            }

            is FlagsType -> {
                val flagsRepresentation = NamedParamType(getNumberTypeRef(wasiType.repr), parameterComment)
                listOf(flagsRepresentation)
            }

            Handle -> {
                val handle = NamedParamType(HANDLE, parameterComment)
                listOf(handle)
            }

            is ListType -> {
                val firstItemPointer = NamedParamType(POINTER, "$parameterComment list first item pointer")
                val listSize = NamedParamType(U32, "$parameterComment list length")
                listOf(firstItemPointer, listSize)
            }

            is RecordType -> {
                val recordPointer = NamedParamType(POINTER, parameterComment)
                listOf(recordPointer)
            }

            is UnionType -> {
                val unionPointer = NamedParamType(POINTER, parameterComment)
                listOf(unionPointer)
            }
        }
    }

    private fun getNumberTypeRef(
        numberType: WasiNumberType,
    ): WasiBaseWasmType = when (numberType) {
        WasiNumberType.SignedNumber.S32 -> WasiBaseWasmType.S32
        WasiNumberType.SignedNumber.S64 -> WasiBaseWasmType.S64
        WasiNumberType.UnsignedNumber.U8 -> WasiBaseWasmType.U8
        WasiNumberType.UnsignedNumber.U16 -> WasiBaseWasmType.U16
        WasiNumberType.UnsignedNumber.U32 -> WasiBaseWasmType.U32
        WasiNumberType.UnsignedNumber.U64 -> WasiBaseWasmType.U64
        else -> error("Unsupported type $numberType")
    }

    fun getFuncInputArgs(
        func: WasiFunc,
    ): List<NamedParamType> {
        val inputParams: List<NamedParamType> = func.params.flatMap { param: WasiFuncParam ->
            getFuncParamBaseTypes(param)
        }
        val expectedData = func.result?.expectedData
        val expectedDataParams = if (expectedData != null) {
            when (expectedData) {
                is ExpectedData.WasiType -> listOf(
                    NamedParamType(POINTER, "expected ${expectedData.identifier}"),
                )

                is Tuple -> listOf(
                    NamedParamType(POINTER, "expected ${expectedData.first}"),
                    NamedParamType(POINTER, "expected ${expectedData.second}"),
                )
            }
        } else {
            emptyList()
        }
        return inputParams + expectedDataParams
    }

    fun getFuncReturnTypes(
        func: WasiFunc,
    ): List<NamedParamType> {
        if (func.result == null) {
            return emptyList()
        }
        return getWasiBaseType(func.result.expectedError, func.result.expectedError)
    }

    internal data class NamedParamType(
        val baseType: WasiBaseWasmType,
        val comment: String,
    )

    internal enum class WasiBaseWasmType {
        POINTER,
        S8,
        U8,
        S16,
        U16,
        S32,
        U32,
        S64,
        U64,
        HANDLE,
    }
}
