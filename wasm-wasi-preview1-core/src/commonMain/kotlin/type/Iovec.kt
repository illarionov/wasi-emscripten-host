/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.preview1.WasiTypename
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32

/**
 * A region of memory for scatter/gather reads.
 *
 * @param buf The address of the buffer to be filled.
 * @param bufLen The length of the buffer to be filled.
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class Iovec(
    @IntWasmPtr(Byte::class)
    val buf: WasmPtr, // (@witx const_pointer u8))
    val bufLen: Size, // (field $buf_len $size)
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = I32
    }
}
