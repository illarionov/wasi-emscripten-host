/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.host.base.binding.WasmFunctionBinding
import at.released.weh.host.emscripten.export.EmscriptenMainExports
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store

internal class ChasmEmscriptenMainExports(
    store: Store,
    instance: Instance,
) : EmscriptenMainExports {
    override val _initialize: WasmFunctionBinding? by optionalFunctionMember(store, instance)
    override val __errno_location: WasmFunctionBinding by functionMember(store, instance)
    override val __wasm_call_ctors: WasmFunctionBinding by functionMember(store, instance)
}
