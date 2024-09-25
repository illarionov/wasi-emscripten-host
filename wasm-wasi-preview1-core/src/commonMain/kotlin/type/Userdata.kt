/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.preview1.WasiTypename
import at.released.weh.wasi.preview1.WasiValueTypes
import at.released.weh.wasm.core.WasmValueType

/**
 * User-provided value that may be attached to objects that is retained when
 * extracted from the implementation.
 */
public class Userdata(
    public val rawValue: Long,
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = WasiValueTypes.U64
    }
}
