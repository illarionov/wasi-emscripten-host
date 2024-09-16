/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType
import kotlin.jvm.JvmInline

/**
 * Flags determining the method of how paths are resolved.
 */
@JvmInline
public value class LookupFlags(
    public val rawMask: UInt,
) {
    public constructor(
        vararg flags: LookupFlag,
    ) : this(
        flags.fold(0U) { acc, flag -> acc.or(flag.mask) },
    )

    public enum class LookupFlag(
        public val mask: UInt,
    ) {
        /**
         * As long as the resolved path corresponds to a symbolic link, it is expanded.
         */
        SYMLINK_FOLLOW(0),

        ;

        constructor(bit: Byte) : this(1U.shl(bit.toInt()))
    }

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U32
    }
}
