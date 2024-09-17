/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.sinkWithMaxSize
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.include.STRUCT_TM_PACKED_SIZE
import at.released.weh.host.include.StructTm
import at.released.weh.host.include.packTo
import kotlinx.io.buffered

public class LocaltimeJsFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.LOCALTIME_JS, host) {
    public fun execute(
        memory: Memory,
        timeSeconds: Long,
        @IntWasmPtr(StructTm::class) timePtr: WasmPtr,
    ) {
        val localTime = host.localTimeFormatter.format(timeSeconds)
        memory.sinkWithMaxSize(timePtr, STRUCT_TM_PACKED_SIZE).buffered().use {
            localTime.packTo(it)
        }
    }
}
