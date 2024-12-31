/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.emcripten.runtime.function.EmscriptenAsmConstAsyncOnMainThreadFunctionHandle
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.WasmFunctionHandle

internal class EmscriptenAsmConstAsyncOnMainThread(host: EmbedderHost) : WasmFunctionHandle {
    private val handle = EmscriptenAsmConstAsyncOnMainThreadFunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Long): LongArray {
        handle.execute(
            args[0].asWasmAddr(),
            args[1].asWasmAddr(),
            args[2].asWasmAddr(),
        )
        return LongArray(0)
    }
}
