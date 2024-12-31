/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.exports

import at.released.weh.bindings.chicory.ext.ChicoryFunctionBindings
import at.released.weh.emcripten.runtime.export.EmscriptenMainExports
import at.released.weh.emcripten.runtime.export.EmscriptenMainExports.Companion.EMSCRIPTEN_MAIN_EXPORT_NAMES
import at.released.weh.wasm.core.WasmFunctionBinding
import com.dylibso.chicory.runtime.Instance

internal class ChicoryEmscriptenMainExports(instance: Instance) : EmscriptenMainExports {
    private val functionBindings = ChicoryFunctionBindings(instance, EMSCRIPTEN_MAIN_EXPORT_NAMES)
    override val _initialize: WasmFunctionBinding? by functionBindings.optional
    override val __errno_location: WasmFunctionBinding? by functionBindings.optional
    override val __wasm_call_ctors: WasmFunctionBinding by functionBindings.required
}
