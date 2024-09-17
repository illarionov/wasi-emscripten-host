/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.ext

import at.released.weh.bindings.graalvm240.host.module.NodeFactory
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunction
import at.released.weh.host.base.function.HostFunction.HostFunctionType
import at.released.weh.wasm.core.WasmValueType
import org.graalvm.wasm.SymbolTable
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmFunction
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmModule

internal fun setupWasmModuleFunctions(
    context: WasmContext,
    host: EmbedderHost,
    module: WasmModule,
    functions: Map<out HostFunction, NodeFactory>,
): WasmInstance {
    val functionTypes: Map<HostFunctionType, Int> = allocateFunctionTypes(module, functions.keys.functionTypes())
    val exportedFunctions: Map<String, WasmFunction> = declareExportedFunctions(module, functionTypes, functions.keys)

    val moduleInstance: WasmInstance = context.readInstance(module)

    functions.forEach { (fn, factory) ->
        val node = factory(context.language(), module, host)
        val exportedIndex = exportedFunctions.getValue(fn.wasmName).index()
        moduleInstance.setTarget(exportedIndex, node.callTarget)
    }
    return moduleInstance
}

internal fun allocateFunctionTypes(
    symbolTable: SymbolTable,
    functionTypes: Collection<HostFunctionType>,
): Map<HostFunctionType, Int> {
    val functionTypeMap: MutableMap<HostFunctionType, Int> = mutableMapOf()
    functionTypes.forEach { type ->
        functionTypeMap.getOrPut(type) {
            val typeIdx = symbolTable.allocateFunctionType(
                type.paramTypes.toTypesByteArray(),
                type.returnTypes.toTypesByteArray(),
                false,
            )
            typeIdx
        }
    }
    return functionTypeMap
}

internal fun List<@WasmValueType Int>.toTypesByteArray(): ByteArray = ByteArray(this.size, Int::toByte)

internal fun declareExportedFunctions(
    symbolTable: SymbolTable,
    functionTypes: Map<HostFunctionType, Int>,
    functions: Collection<HostFunction>,
): Map<String, WasmFunction> {
    return functions.associate { fn ->
        val typeIdx = functionTypes.getValue(fn.type)
        val functionIdx = symbolTable.declareExportedFunction(typeIdx, fn.wasmName)
        fn.wasmName to functionIdx
    }
}

private fun Collection<HostFunction>.functionTypes(): Set<HostFunctionType> = mapTo(mutableSetOf(), HostFunction::type)
