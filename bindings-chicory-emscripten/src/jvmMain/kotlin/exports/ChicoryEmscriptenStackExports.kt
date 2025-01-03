/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.exports

import at.released.weh.bindings.chicory.ext.ChicoryFunctionBindings
import at.released.weh.bindings.chicory.ext.ChicoryIntGlobalsBindings
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStackExports
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStackExports.Companion.EMSCRIPTEN_STACK_EXPORTED_FUNCTION_NAMES
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStackExports.Companion.EMSCRIPTEN_STACK_EXPORTED_GLOBAL_NAMES
import at.released.weh.wasm.core.WasmFunctionBinding
import com.dylibso.chicory.runtime.Instance

internal class ChicoryEmscriptenStackExports(instance: Instance) : EmscriptenStackExports {
    private val functionBindings = ChicoryFunctionBindings(instance, EMSCRIPTEN_STACK_EXPORTED_FUNCTION_NAMES)
    private val globalBindings = ChicoryIntGlobalsBindings(instance, EMSCRIPTEN_STACK_EXPORTED_GLOBAL_NAMES)
    override var __stack_pointer: Int by globalBindings.required
    override var __stack_end: Int? by globalBindings.optional
    override var __stack_base: Int? by globalBindings.optional
    override val __set_stack_limits: WasmFunctionBinding? by functionBindings.optional
    override val emscripten_stack_init by functionBindings.optional
    override val emscripten_stack_get_free by functionBindings.optional
    override val emscripten_stack_get_base by functionBindings.optional
    override val emscripten_stack_get_end by functionBindings.optional
    override val emscripten_stack_get_current by functionBindings.required
    override val emscripten_stack_set_limits by functionBindings.optional
    override val _emscripten_stack_alloc by functionBindings.required
    override val _emscripten_stack_restore by functionBindings.required
}
