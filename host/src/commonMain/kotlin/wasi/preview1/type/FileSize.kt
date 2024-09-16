/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType

/**
 * Non-negative file size or length of a region within a file.
 */
public data class FileSize(
    public val value: Long,
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = WasiValueTypes.U64
    }
}
