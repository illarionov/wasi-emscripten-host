/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.exports

import at.released.weh.bindings.chicory.ext.functionMember
import at.released.weh.bindings.chicory.ext.intGlobalMember
import at.released.weh.bindings.chicory.ext.optionalFunctionMember
import at.released.weh.bindings.chicory.ext.optionalIntGlobalMember
import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.stack.EmscriptenStackExports
import com.dylibso.chicory.runtime.Instance

internal class ChicoryEmscriptenStackExports(instance: Instance) : EmscriptenStackExports {
    override var __stack_pointer: Int by instance.intGlobalMember()
    override var __stack_end: Int? by instance.optionalIntGlobalMember()
    override var __stack_base: Int? by instance.optionalIntGlobalMember()
    override val __set_stack_limits: WasmFunctionBinding? by instance.optionalFunctionMember()
    override val emscripten_stack_init by instance.optionalFunctionMember()
    override val emscripten_stack_get_free: WasmFunctionBinding? by instance.optionalFunctionMember()
    override val emscripten_stack_get_base: WasmFunctionBinding? by instance.optionalFunctionMember()
    override val emscripten_stack_get_end: WasmFunctionBinding? by instance.optionalFunctionMember()
    override val emscripten_stack_get_current: WasmFunctionBinding by instance.functionMember()
    override val emscripten_stack_set_limits: WasmFunctionBinding by instance.functionMember()
    override val _emscripten_stack_alloc: WasmFunctionBinding by instance.functionMember()
    override val _emscripten_stack_restore: WasmFunctionBinding by instance.functionMember()
}
