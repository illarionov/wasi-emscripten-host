/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import kotlin.jvm.JvmInline

/**
 * Flags provided to `sock_recv`.
 */
@JvmInline
public value class Riflags(
    public val rawMask: UShort,
) {
    public constructor(
        vararg flags: RiFlagsValue,
    ) : this(
        flags.fold(0.toUShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class RiFlagsValue(
        public val mask: UShort,
    ) {
        /**
         * Returns the message without removing it from the socket's receive queue.
         */
        RECV_PEEK(0),

        /**
         * On byte-stream sockets, block until the full amount of data can be returned.
         */
        RECV_WAITALL(1),

        ;

        constructor(bit: Int) : this(1.shl(bit).toUShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
