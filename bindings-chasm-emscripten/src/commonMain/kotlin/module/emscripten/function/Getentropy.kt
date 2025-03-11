/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.emcripten.runtime.function.GetentropyFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.runtime.value.NumberValue

internal class Getentropy(
    host: EmbedderHost,
    private val memory: Memory,
) : HostFunctionProvider {
    private val handle = GetentropyFunctionHandle(host)
    override val function: HostFunction = { args ->
        val code = handle.execute(
            memory,
            args[0].asWasmAddr(),
            args[1].asInt(),
        )
        listOf(NumberValue.I32(code))
    }
}
