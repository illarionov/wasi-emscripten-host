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
 * Flags determining how to interpret the timestamp provided in `subscription_clock::timeout`.
 */
public data class Subclockflags(
    public val rawMask: Short,
) {
    public constructor(
        vararg flags: Subclockflags,
    ) : this(
        flags.fold(0.toShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Subclockflags(
        public val mask: Short,
    ) {
        /**
         * If set, treat the timestamp provided in
         * `subscription_clock::timeout` as an absolute timestamp of clock
         * `subscription_clock::id`. If clear, treat the timestamp
         * provided in `subscription_clock::timeout` relative to the
         * current time value of clock `subscription_clock::id`.
         */
        SUBSCRIPTION_CLOCK_ABSTIME(0),

        ;

        constructor(bit: Int) : this(1.shl(bit).toShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
