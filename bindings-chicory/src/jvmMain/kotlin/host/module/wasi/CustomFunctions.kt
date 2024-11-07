/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.wasi

import at.released.weh.bindings.chicory.ProcExitException
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.WasmFunctionHandle
import com.dylibso.chicory.wasm.types.ValueType.I32

internal fun createCustomChicoryFunctions(
    moduleName: String = "wasi_snapshot_preview1",
): List<HostFunction> = listOf(
    HostFunction(moduleName, "proc_exit", listOf(I32), emptyList(), procExit),
)

private val procExit: WasmFunctionHandle = WasmFunctionHandle { _, args ->
    val exitCode = args[0].toInt()
    throw ProcExitException(exitCode)
}
