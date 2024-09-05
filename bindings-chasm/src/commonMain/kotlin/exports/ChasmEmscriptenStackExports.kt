/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.stack.EmscriptenStackExports
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store

internal class ChasmEmscriptenStackExports(
    store: Store,
    instance: Instance,
) : EmscriptenStackExports {
    override var __stack_pointer: Int by intGlobalMember(store, instance)
    override var __stack_end: Int? by optionalIntGlobalMember(store, instance)
    override var __stack_base: Int? by optionalIntGlobalMember(store, instance)
    override val __set_stack_limits: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val emscripten_stack_init: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val emscripten_stack_get_free: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val emscripten_stack_get_base: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val emscripten_stack_get_end: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val emscripten_stack_get_current: WasmFunctionBinding by functionMember(store, instance)
    override val emscripten_stack_set_limits: WasmFunctionBinding by functionMember(store, instance)
    override val _emscripten_stack_alloc: WasmFunctionBinding by functionMember(store, instance)
    override val _emscripten_stack_restore: WasmFunctionBinding by functionMember(store, instance)
}
