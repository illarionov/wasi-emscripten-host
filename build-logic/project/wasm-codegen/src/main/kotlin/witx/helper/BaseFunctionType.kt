/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.helper

import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I32
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.BaseWebAssemblyType.I64
import at.released.weh.gradle.wasm.codegen.witx.helper.BaseFunctionType.ListOfBaseWebAssemblyTypes.listOfTypesComparator
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.NamedParamType
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
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
internal data class BaseFunctionType(
    val input: List<BaseWebAssemblyType>,
    val results: List<BaseWebAssemblyType>,
) : Comparable<BaseFunctionType> {
    val propertyName: String = buildString {
        append("type_")

        if (input.isNotEmpty()) {
            input.joinTo(this, separator = "") { it.shortname }
        } else {
            append("n")
        }

        append("_")

        if (results.isNotEmpty()) {
            results.joinTo(this, separator = "") { it.shortname }
        } else {
            append("n")
        }
    }

    override fun compareTo(other: BaseFunctionType): Int {
        listOfTypesComparator.compare(input, other.input).let {
            if (it != 0) {
                return it
            }
        }
        listOfTypesComparator.compare(results, other.results).let {
            if (it != 0) {
                return it
            }
        }
        return 0
    }

    internal object ListOfBaseWebAssemblyTypes {
        internal val listOfTypesComparator: Comparator<List<BaseWebAssemblyType>> =
            compareBy(List<BaseWebAssemblyType>::size)
                .thenComparing { types1: List<BaseWebAssemblyType>, types2: List<BaseWebAssemblyType> ->
                    types1.zip(types2, ::compareValues).firstOrNull { it != 0 } ?: 0
                }

        internal fun List<BaseWebAssemblyType>.listPropertyName(): String = if (this.isNotEmpty()) {
            joinToString(prefix = "list_", separator = "") { it.shortname }
        } else {
            "list_empty"
        }

        internal fun getListInstances(
            func: List<WasiFunc>,
            typeResolver: WasiBaseTypeResolver,
        ): Set<List<BaseWebAssemblyType>> {
            return func.flatMap { wasiFunc ->
                listOf(
                    typeResolver.getFuncInputArgs(wasiFunc),
                    typeResolver.getFuncReturnTypes(wasiFunc),
                )
            }
                .map { args: List<NamedParamType> -> args.map { it.baseType.baseType } }
                .toSortedSet(listOfTypesComparator)
        }
    }

    internal enum class BaseWebAssemblyType {
        I32,
        I64,
    }

    internal companion object {
        internal val BaseWebAssemblyType.shortname: String
            get() = when (this) {
                I32 -> "i"
                I64 -> "j"
            }

        internal val WasiBaseWasmType.baseType: BaseWebAssemblyType
            get() = when (this) {
                POINTER, HANDLE, S8, U8, S16, U16, S32, U32 -> BaseWebAssemblyType.I32
                S64, U64 -> BaseWebAssemblyType.I64
            }

        internal fun fromWasiFunc(
            func: WasiFunc,
            typeResolver: WasiBaseTypeResolver,
        ): BaseFunctionType = BaseFunctionType(
            input = typeResolver.getFuncInputArgs(func).map { it.baseType.baseType },
            results = typeResolver.getFuncReturnTypes(func).map { it.baseType.baseType },
        )
    }
}
