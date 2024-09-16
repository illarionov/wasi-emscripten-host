/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import at.released.weh.host.base.WasmValueTypes.I64

/**
 * Relative offset within a file.
 *
 * (typename $filedelta s64)
 */
public data class FileDelta(
    public val rawValue: Long,
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = I64
    }
}
