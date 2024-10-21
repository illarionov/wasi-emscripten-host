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

public class ClockTimeGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.CLOCK_TIME_GET, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        id: Int,
        precision: Long,
        @IntWasmPtr(Timestamp::class) timestampAddr: WasmPtr,
    ): Errno {
        val clockId = Clockid.fromCode(id) ?: return Errno.INVAL

        val timestampNs = when (clockId) {
            REALTIME -> host.clock.getCurrentTimeEpochNanoseconds()
            MONOTONIC -> host.monotonicClock.getTimeMarkNanoseconds()
            PROCESS_CPUTIME_ID, THREAD_CPUTIME_ID -> host.cputimeSource.getClock(clockId.hostClockId).let {
                if (!it.isSupported) {
                    return Errno.NOTSUP
                }
                it.getTimeMarkNanoseconds()
            }
        }
        memory.writeU64(timestampAddr, timestampNs.toULong())

        return Errno.SUCCESS
    }
}
