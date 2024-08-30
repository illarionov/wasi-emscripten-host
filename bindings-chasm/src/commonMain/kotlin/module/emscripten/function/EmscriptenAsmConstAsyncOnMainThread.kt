/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.EmscriptenAsmConstAsyncOnMainThreadFunctionHandle
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue

internal class EmscriptenAsmConstAsyncOnMainThread(host: EmbedderHost) : EmscriptenHostFunctionHandle {
    private val handle = EmscriptenAsmConstAsyncOnMainThreadFunctionHandle(host)

    override fun invoke(args: List<ExecutionValue>): List<ExecutionValue> {
        handle.execute(
            emAsmAddr = args[0].asWasmAddr(),
            sigPtr = args[1].asWasmAddr(),
            argbuf = args[2].asWasmAddr(),
        )
        return emptyList()
    }
}
