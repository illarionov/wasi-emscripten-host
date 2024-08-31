/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.function.SyscallReadlinkatFunctionHandle
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class SyscallReadlinkat(
    host: EmbedderHost,
    private val memory: Memory,
) : EmscriptenHostFunctionHandle {
    private val handle = SyscallReadlinkatFunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Value): Value? {
        val sizeOrErrno = handle.execute(
            memory,
            rawDirFd = args[0].asInt(),
            pathnamePtr = args[1].asWasmAddr(),
            buf = args[2].asWasmAddr(),
            bufSize = args[3].asInt(),
        )
        return Value.i32(sizeOrErrno.toLong())
    }
}
