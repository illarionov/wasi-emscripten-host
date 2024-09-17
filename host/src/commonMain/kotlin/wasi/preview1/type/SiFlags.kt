/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType

/**
 * Flags provided to `sock_send`. As there are currently no flags
 * defined, it must be set to zero.
 */
public data class SiFlags(
    public val rawValue: Int,
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = WasiValueTypes.U16
    }
}
