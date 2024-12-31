/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.wasip1

import at.released.weh.bindings.chasm.exception.ProcExitException
import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.wasm.core.WasmModules
import io.github.charlietap.chasm.embedding.function
import io.github.charlietap.chasm.embedding.shapes.FunctionType
import io.github.charlietap.chasm.embedding.shapes.Import
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.ValueType.Number.I32
import io.github.charlietap.chasm.embedding.shapes.HostFunction as ChasmHostFunction

internal fun createCustomWasiPreview1HostFunctions(
    store: Store,
    moduleName: String = WasmModules.WASI_SNAPSHOT_PREVIEW1_MODULE_NAME,
): List<Import> {
    return listOf(
        Import(
            moduleName,
            "proc_exit",
            function(store, FunctionType(listOf(I32), listOf()), procExitHostFunction),
        ),
    )
}

private val procExitHostFunction: ChasmHostFunction = { args ->
    val exitCode = args[0].asInt()
    // TODO: throw HostFunctionException on Chasm 0.9.3+
    // throw HostFunctionException(exitCode.toString())
    throw ProcExitException(exitCode)
}
