/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import kotlin.experimental.or

/**
 * The state of the file descriptor subscribed to with
 * `eventtype::fd_read` or `eventtype::fd_write`.
 */
public class Eventrwflags(
    public val rawMask: Short,
) {
    public constructor(
        vararg flags: Eventrwflags,
    ) : this(
        flags.fold(0.toShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Eventrwflags(
        public val mask: Short,
    ) {
        /**
         * The peer of this socket has closed or disconnected.
         */
        FD_READWRITE_HANGUP(0),

        ;

        constructor(bit: Int) : this(1.shl(bit).toShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}