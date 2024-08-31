/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.exports

import at.released.weh.host.base.binding.IndirectFunctionBindingProvider
import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.base.function.IndirectFunctionTableIndex
import org.graalvm.polyglot.Value

internal class GraalvmIndirectFunctionProvider(
    @Suppress("UnusedPrivateProperty") mainBindings: () -> Value,
) : IndirectFunctionBindingProvider {
    override fun getFunctionBinding(indirectId: IndirectFunctionTableIndex): WasmFunctionBinding {
        error("Not implemented")
    }
}
