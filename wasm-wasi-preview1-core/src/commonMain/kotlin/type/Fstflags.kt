/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import kotlin.experimental.or

/**
 *  Which file time attributes to adjust.
 */
public data class Fstflags(
    public val rawMask: Short,
) {
    public constructor(
        vararg flags: Fstflags,
    ) : this(
        flags.fold(0.toShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Fstflags(
        public val mask: Short,
    ) {
        /**
         * Adjust the last data access timestamp to the value stored in `filestat::atim`.
         */
        ATIM(0),

        /**
         * Adjust the last data access timestamp to the time of clock `clockid::realtime`.
         */
        ATIM_NOW(1),

        /**
         * Adjust the last data modification timestamp to the value stored in `filestat::mtim`.
         */
        MTIM(2),

        /**
         * Adjust the last data modification timestamp to the time of clock `clockid::realtime`.
         */
        MTIM_NOW(3),

        ;

        constructor(bit: Int) : this(1L.shl(bit).toShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
