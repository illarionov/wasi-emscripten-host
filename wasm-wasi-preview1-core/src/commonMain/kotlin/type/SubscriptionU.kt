/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32

/**
 * The contents of a `subscription`.
 */
public sealed class SubscriptionU(
    public open val eventType: Eventtype,
) {
    public data class FdRead(
        val subscriptionFdReadwrite: SubscriptionFdReadwrite,
    ) : SubscriptionU(Eventtype.FD_READ)

    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = I32
    }
    public data class Clock(
        val subscriptionClock: SubscriptionClock,
    ) : SubscriptionU(Eventtype.CLOCK)

    public data class FdWrite(
        val subscriptionFdReadwrite: SubscriptionFdReadwrite,
    ) : SubscriptionU(Eventtype.FD_WRITE)
}
