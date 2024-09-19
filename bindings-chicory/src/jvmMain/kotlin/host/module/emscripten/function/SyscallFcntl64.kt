/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.SyscallFcntl64FunctionHandle
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class SyscallFcntl64(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : EmscriptenHostFunctionHandle {
    private val handle = SyscallFcntl64FunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Value): Value? {
        val result: Int = handle.execute(
            memory,
            args[0].asInt(),
            args[1].asInt(),
            args[2].asInt(),
        )
        return Value.i32(result.toLong())
    }
}
