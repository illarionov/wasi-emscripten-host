/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.exports

import at.released.weh.bindings.graalvm240.ext.optionalFunctionMember
import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.memory.DynamicMemoryExports
import org.graalvm.polyglot.Value

internal class GraalvmDynamicMemoryExports(
    mainBindings: () -> Value,
) : DynamicMemoryExports {
    override val malloc: WasmFunctionBinding? by mainBindings.optionalFunctionMember()
    override val free: WasmFunctionBinding? by mainBindings.optionalFunctionMember()
}