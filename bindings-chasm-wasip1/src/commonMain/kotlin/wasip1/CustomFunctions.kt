/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.wasip1

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.wasm.core.WasmModules
import io.github.charlietap.chasm.embedding.function
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.host.HostFunctionException
import io.github.charlietap.chasm.runtime.value.ExecutionValue
import io.github.charlietap.chasm.type.FunctionType
import io.github.charlietap.chasm.type.NumberType.I32
import io.github.charlietap.chasm.type.ResultType
import io.github.charlietap.chasm.type.ValueType
import io.github.charlietap.chasm.embedding.shapes.HostFunction as ChasmHostFunction

internal fun createCustomWasiPreview1HostFunctions(
    store: Store,
    moduleName: String = WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
): List<Import> {
    return listOf(
        Import(
            moduleName,
            "proc_exit",
            function(
                store,
                FunctionType(
                    ResultType(listOf(ValueType.Number(I32))),
                    ResultType(listOf()),
                ),
                procExitHostFunction,
            ),
        ),
    )
}

private val procExitHostFunction: ChasmHostFunction = { args: List<ExecutionValue> ->
    val exitCode = args[0].asInt()
    throw HostFunctionException(exitCode.toString())
}
