/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.MunapJsFunctionHandle
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class MunmapJs(host: EmbedderHost) : EmscriptenHostFunctionHandle {
    private val handle = MunapJsFunctionHandle(host)

    @Suppress("MagicNumber")
    override fun apply(instance: Instance, vararg args: Value): Value? {
        val result: Int = handle.execute(
            args[0].asWasmAddr(),
            args[1].asInt(),
            args[2].asInt(),
            args[3].asInt(),
            args[4].asInt(),
            args[5].asLong(),
        )
        return Value.i32(result.toLong())
    }
}
