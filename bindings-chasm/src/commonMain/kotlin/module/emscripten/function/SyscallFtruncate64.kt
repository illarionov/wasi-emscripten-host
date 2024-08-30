/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.bindings.chasm.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.SyscallFtruncate64FunctionHandle
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32

internal class SyscallFtruncate64(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle {
    private val handle = SyscallFtruncate64FunctionHandle(host)

    override fun invoke(args: List<ExecutionValue>): List<ExecutionValue> {
        val result: Int = handle.execute(
            Fd(args[0].asInt()),
            args[1].asLong().toULong(),
        )
        return listOf(I32(result))
    }
}
