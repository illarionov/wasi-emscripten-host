/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.sinkWithMaxSize
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.writeNullTerminatedString
import kotlinx.io.buffered

public class TzsetJsFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.TZSET_JS, host) {
    public fun execute(
        memory: Memory,
        timezone: WasmPtr<Int>,
        daylight: WasmPtr<Int>,
        stdName: WasmPtr<Byte>,
        dstName: WasmPtr<Byte>,
    ) {
        val tzInfo = host.timeZoneInfo.getTimeZoneInfo()
        logger.v { "tzsetJs() TZ info: $tzInfo" }
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
