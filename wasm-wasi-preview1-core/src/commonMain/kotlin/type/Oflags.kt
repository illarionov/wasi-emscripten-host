/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import kotlin.experimental.or

/**
 * Open flags used by `path_open`.
 */
public data class Oflags(
    public val rawMask: Short,
) {
    public constructor(
        vararg flags: Oflags,
    ) : this(
        flags.fold(0.toShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Oflags(
        public val mask: Short,
    ) {
        /**
         * Create file if it does not exist.
         */
        CREAT(0),

        /**
         * Fail if not a directory.
         */
        DIRECTORY(1),

        /**
         * Fail if file already exists.
         */
        EXCL(2),

        /**
         * Truncate file to size 0.
         */
        TRUNC(3),

        ;

        constructor(bit: Int) : this(1.shl(bit).toShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
