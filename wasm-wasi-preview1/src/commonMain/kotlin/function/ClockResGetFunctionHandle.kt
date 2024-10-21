/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.hostClockId
import at.released.weh.wasi.preview1.type.Clockid
import at.released.weh.wasi.preview1.type.Clockid.MONOTONIC
import at.released.weh.wasi.preview1.type.Clockid.PROCESS_CPUTIME_ID
import at.released.weh.wasi.preview1.type.Clockid.REALTIME
import at.released.weh.wasi.preview1.type.Clockid.THREAD_CPUTIME_ID
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.writeU64

public class ClockResGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.CLOCK_RES_GET, host) {
    public fun execute(
        memory: Memory,
        id: Int,
        @IntWasmPtr(Timestamp::class) timestampAddr: WasmPtr,
    ): Errno {
        val clockId = Clockid.fromCode(id) ?: return Errno.INVAL

        val resolutionNs = when (clockId) {
            REALTIME -> host.clock.getResolutionNanoseconds()
            MONOTONIC -> host.monotonicClock.getResolutionNanoseconds()
            PROCESS_CPUTIME_ID, THREAD_CPUTIME_ID -> host.cputimeSource.getClock(clockId.hostClockId).let {
                if (!it.isSupported) {
                    return Errno.NOTSUP
                }
                it.getResolutionNanoseconds()
            }
        }
        memory.writeU64(timestampAddr, resolutionNs.toULong())

        return Errno.SUCCESS
    }
}