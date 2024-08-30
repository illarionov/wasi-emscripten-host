/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asUInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.emscripten.function.SyscallUtimensatFunctionHandle
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32

internal class SyscallUtimensat(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : EmscriptenHostFunctionHandle {
    private val handle = SyscallUtimensatFunctionHandle(host)

    override fun invoke(args: List<ExecutionValue>): List<ExecutionValue> {
        val result = handle.execute(
            memory,
            args[0].asInt(),
            args[1].asWasmAddr(),
            args[2].asWasmAddr(),
            args[3].asUInt(),
        )
        return listOf(I32(result))
    }
}
