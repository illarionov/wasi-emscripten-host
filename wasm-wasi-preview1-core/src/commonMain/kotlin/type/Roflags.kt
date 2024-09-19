/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.preview1.WasiTypename
import at.released.weh.wasm.core.WasmValueType
import kotlin.experimental.or

/**
 * Flags returned by `sock_recv`.
 */
public data class Roflags(
    public val rawMask: Short,
) {
    public constructor(
        vararg flags: RoflagsValue,
    ) : this(
        flags.fold(0.toShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class RoflagsValue(
        public val mask: Short,
    ) {
        /**
         * Returned by `sock_recv`: Message data has been truncated.
         */
        RECV_DATA_TRUNCATED(0),

        ;

        constructor(bit: Int) : this(1.shl(bit).toShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
