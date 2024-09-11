/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.emscripten.function.EmscriptenConsoleErrorFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class EmscriptenConsoleError(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : HostFunction {
    private val handle = EmscriptenConsoleErrorFunctionHandle(host)

    override fun invoke(args: List<Value>): List<Value> {
        handle.execute(
            memory = memory,
            messagePtr = args[0].asWasmAddr(),
        )
        return emptyList()
    }
}