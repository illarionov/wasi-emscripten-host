/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("PropertyName", "VariableNaming")

package at.released.weh.emcripten.runtime.export

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.WasmFunctionBinding

/**
 * Interface for calling Emscripten exported functions
 */
public interface EmscriptenMainExports {
    public val _initialize: WasmFunctionBinding?
    public val __errno_location: WasmFunctionBinding?
    public val __wasm_call_ctors: WasmFunctionBinding

    public companion object {
        @InternalWasiEmscriptenHostApi
        public val EMSCRIPTEN_MAIN_EXPORT_NAMES: Set<String> = setOf(
            "_initialize",
            "__errno_location",
            "__wasm_call_ctors",
        )
    }
}
