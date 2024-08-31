/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.exports

import at.released.weh.bindings.chicory.ext.functionMember
import at.released.weh.bindings.chicory.ext.optionalFunctionMember
import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.EmscriptenMainExports
import com.dylibso.chicory.runtime.Instance

internal class ChicoryEmscriptenMainExports(instance: Instance) : EmscriptenMainExports {
    override val _initialize: WasmFunctionBinding? by instance.optionalFunctionMember()
    override val __errno_location: WasmFunctionBinding by instance.functionMember()
    override val __wasm_call_ctors: WasmFunctionBinding by instance.functionMember()
}
