/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import kotlin.jvm.JvmInline

/**
 *  Which file time attributes to adjust.
 */
@JvmInline
public value class Fstflags(
    public val rawMask: UShort,
) {
    public constructor(
        vararg flags: Fstflags,
    ) : this(
        flags.fold(0.toUShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Fstflags(
        public val mask: UShort,
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

        constructor(bit: Int) : this(1UL.shl(bit).toUShort())
    }

    public companion object : WasiTypename {
        override val wasmValueType: WasmValueType = WasiValueTypes.U16
    }
}
