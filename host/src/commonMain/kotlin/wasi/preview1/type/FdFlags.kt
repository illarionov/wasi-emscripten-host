/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import kotlin.jvm.JvmInline

/**
 * File descriptor flags.
 */
@JvmInline
public value class FdFlags(
    public val rawMask: UShort,
) {
    public constructor(
        vararg flags: Flags,
    ) : this(
        flags.fold(0.toUShort()) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class Flags(
        public val mask: UShort,
    ) {
        /**
         * Append mode: Data written to the file is always appended to the file's end.
         */
        APPEND(0),

        /**
         * Write according to synchronized I/O data integrity completion. Only the data stored in the file
         * is synchronized.
         */
        DSYNC(1),

        /**
         * Non-blocking mode.
         */
        NONBLOCK(2),

        /**
         * Synchronized read I/O operations.
         */
        RSYNC(3),

        /**
         * Write according to synchronized I/O file integrity completion. In
         * addition to synchronizing the data stored in the file, the implementation
         * may also synchronously update the file's metadata.
         */
        SYNC(4),

        ;

        constructor(bit: Int) : this(1.shl(bit).toUShort())
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U16
    }
}
