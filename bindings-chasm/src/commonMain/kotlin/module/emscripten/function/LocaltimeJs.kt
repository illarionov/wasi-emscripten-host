/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.function.LocaltimeJsFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction

internal class LocaltimeJs(
    host: EmbedderHost,
    private val memory: Memory,
) : HostFunctionProvider {
    private val handle = LocaltimeJsFunctionHandle(host)
    override val function: HostFunction = { args ->
        handle.execute(memory, args[0].asLong(), args[1].asWasmAddr())
        emptyList()
    }
}
