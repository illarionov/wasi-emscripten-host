/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.emcripten.runtime.function.SyscallFchown32FunctionHandle
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class SyscallFchown32(host: EmbedderHost) : EmscriptenHostFunctionHandle {
    private val handle = SyscallFchown32FunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Value): Value? {
        val result: Int = handle.execute(
            args[0].asInt(),
            args[1].asInt(),
            args[2].asInt(),
        )
        return Value.i32(result.toLong())
    }
}
