/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.host.base.WasmPtr.Companion.C_NULL
import kotlin.jvm.JvmInline

public class WasmPtr<out P : Any?>(
    public val addr: Int,
) {
    override fun toString(): String = "0x${addr.toString(16)}"
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as WasmPtr<*>

        return addr == other.addr
    }

    override fun hashCode(): Int {
        return addr
    }

    @Suppress("UNCHECKED_CAST")
    @InternalWasiEmscriptenHostApi
    public companion object {
        public const val WASM_SIZEOF_PTR: UInt = 4U
        public val C_NULL: WasmPtr<*> = WasmPtr<Unit>(0)
        public fun <P> cNull(): WasmPtr<P> = C_NULL as WasmPtr<P>
    }
}

@InternalWasiEmscriptenHostApi
public fun WasmPtr<*>.isNull(): Boolean = this == C_NULL

@InternalWasiEmscriptenHostApi
public operator fun <P> WasmPtr<P>.plus(bytes: Int): WasmPtr<P> = WasmPtr(addr + bytes)
