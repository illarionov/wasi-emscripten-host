/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.TZSET_JS
import at.released.weh.host.EmbedderHost
import at.released.weh.host.ext.writeNullTerminatedString
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class TzsetJsFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(TZSET_JS, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Int::class) timezone: WasmPtr,
        @IntWasmPtr(Int::class) daylight: WasmPtr,
        @IntWasmPtr(Byte::class) stdName: WasmPtr,
        @IntWasmPtr(Byte::class) dstName: WasmPtr,
    ) {
        val tzInfo = host.timeZoneInfo.getTimeZoneInfo()
        memory.writeI32(timezone, tzInfo.timeZone.toInt())
        memory.writeI32(daylight, tzInfo.daylight)

        memory.sinkWithMaxSize(stdName, TZ_NAME_MAX_SIZE).buffered().use {
            it.writeNullTerminatedString(tzInfo.stdName, TZ_NAME_MAX_SIZE)
        }

        memory.sinkWithMaxSize(dstName, TZ_NAME_MAX_SIZE).buffered().use {
            it.writeNullTerminatedString(tzInfo.dstName, TZ_NAME_MAX_SIZE)
        }
    }

    private companion object {
        private const val TZ_NAME_MAX_SIZE = 17
    }
}
