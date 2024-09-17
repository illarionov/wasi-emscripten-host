/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType

/**
 * Identifiers for clocks.
 */
public enum class ClockId(
    public val id: Int,
) {
    /**
     * The clock measuring real time. Time value zero corresponds with 1970-01-01T00:00:00Z.
     */
    REALTIME(0),

    /**
     * The store-wide monotonic clock, which is defined as a clock measuring
     * real time, whose value cannot be adjusted and which cannot have negative
     * clock jumps. The epoch of this clock is undefined. The absolute time
     * value of this clock therefore has no meaning.
     */
    MONOTONIC(1),

    /**
     * The CPU-time clock associated with the current process.
     */
    PROCESS_CPUTIME_ID(2),

    /**
     * The CPU-time clock associated with the current thread.
     */
    THREAD_CPUTIME_ID(3),

    ;

    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = WasiValueTypes.U32
    }
}
