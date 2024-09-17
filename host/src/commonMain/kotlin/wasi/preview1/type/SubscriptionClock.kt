/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32

/**
 * The contents of a `subscription` when type is `eventtype::clock`.
 *
 * @param id The clock against which to compare the timestamp.
 * @param timeout The absolute or relative timestamp.
 * @param precision The amount of time that the implementation may wait additionally to coalesce with other events.
 * @param flags Flags specifying whether the timeout is absolute or relative
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class SubscriptionClock(
    val id: ClockId, // (field $id $clockid)
    val timeout: Timestamp, // (field $timeout $timestamp)
    val precision: Timestamp, // (field $precision $timestamp)
    val flags: Subclockflags, // (field $flags $subclockflags)
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = I32
    }
}
