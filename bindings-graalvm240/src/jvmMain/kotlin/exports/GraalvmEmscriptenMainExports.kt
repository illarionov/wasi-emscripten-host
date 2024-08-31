/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.exports

import at.released.weh.bindings.graalvm240.ext.functionMember
import at.released.weh.bindings.graalvm240.ext.optionalFunctionMember
import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.EmscriptenMainExports
import org.graalvm.polyglot.Value

internal class GraalvmEmscriptenMainExports(
    mainBindings: () -> Value,
) : EmscriptenMainExports {
    override val _initialize: WasmFunctionBinding? by mainBindings.optionalFunctionMember()
    override val __errno_location: WasmFunctionBinding by mainBindings.functionMember()
    override val __wasm_call_ctors: WasmFunctionBinding by mainBindings.functionMember()
}
