/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Event
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasi.preview1.type.SizeType
import at.released.weh.wasi.preview1.type.Subscription
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PollOneoffFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.POLL_ONEOFF, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Subscription::class) inSubscriptionPtr: WasmPtr,
        @IntWasmPtr(Event::class) outEventsPtr: WasmPtr,
        @SizeType subscriptionCount: Size,
        @IntWasmPtr(Int::class) eventsStoredAddr: WasmPtr,
    ): Errno {
        // TODO
        return Errno.NOTSUP
    }
}
