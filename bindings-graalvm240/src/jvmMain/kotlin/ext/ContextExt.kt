/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.ext

import org.graalvm.polyglot.Context
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmTable

internal inline fun <R> Context.withWasmContext(
    code: (wasmContext: WasmContext) -> R,
): R = try {
    enter()
    code(WasmContext.get(null))
} finally {
    leave()
}

internal val WasmContext.functionTable: WasmTable
    get() = this.tables().table(0)
