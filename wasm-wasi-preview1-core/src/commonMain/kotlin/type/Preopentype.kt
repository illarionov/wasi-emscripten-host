/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.preview1.WasiTypename
import at.released.weh.wasi.preview1.type.WasiValueTypes.U8
import at.released.weh.wasm.core.WasmValueType

/**
 * Identifiers for preopened capabilities.
 */
public enum class Preopentype(
    public val value: Int,
) {
    /**
     * A pre-opened directory.
     */
    DIR(0),

    ;

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = U8
    }
}
