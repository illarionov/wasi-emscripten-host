/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import at.released.weh.host.wasi.preview1.type.WasiValueTypes.U8
import kotlin.experimental.or

/**
 * Which channels on a socket to shut down.
 */
public data class Sdflags(
    public val rawMask: Byte,
) {
    public constructor(
        vararg flags: Sdflags,
    ) : this(
        flags.fold(0.toByte()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Sdflags(
        public val mask: Byte,
    ) {
        /**
         * Disables further receive operations.
         */
        RD(0),

        /**
         * Disables further send operations.
         */
        WR(1),

        ;

        constructor(bit: Int) : this(1.shl(bit).toByte())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = U8
    }
}
