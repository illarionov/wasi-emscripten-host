/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.AssertFailFunctionHandle
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import io.github.charlietap.chasm.embedding.shapes.HostFunction

internal class AssertFail(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : HostFunctionProvider {
    private val handle = AssertFailFunctionHandle(host)
    override val function: HostFunction = { args ->
        handle.execute(
            memory = memory,
            condition = args[0].asWasmAddr(),
            filename = args[1].asWasmAddr(),
            line = args[2].asInt(),
            func = args[3].asWasmAddr(),
        )
    }
}
