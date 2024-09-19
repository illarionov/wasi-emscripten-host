/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NULLABLE_PROPERTY_TYPE")

package at.released.weh.bindings.chasm.exports

import at.released.weh.emcripten.runtime.export.EmscriptenMainExports
import at.released.weh.emcripten.runtime.export.EmscriptenMainExports.Companion.EMSCRIPTEN_MAIN_EXPORT_NAMES
import at.released.weh.host.base.binding.WasmFunctionBinding
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store

internal class ChasmEmscriptenMainExports(
    store: Store,
    instance: Instance,
) : EmscriptenMainExports {
    private val functionBindings = ChasmFunctionBindings(
        store = store,
        instance = instance,
        exportNames = EMSCRIPTEN_MAIN_EXPORT_NAMES,
    )
    override val _initialize: WasmFunctionBinding? by functionBindings.optional
    override val __errno_location: WasmFunctionBinding? by functionBindings.optional
    override val __wasm_call_ctors: WasmFunctionBinding by functionBindings.required
}
