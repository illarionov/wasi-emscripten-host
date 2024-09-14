/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.function.SyscallStatLstat64FunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal fun syscallStat64(
    host: EmbedderHost,
    memory: Memory,
): HostFunctionProvider = SyscallStat64Lstat64(memory, SyscallStatLstat64FunctionHandle.syscallStat64(host))

internal fun syscallLstat64(
    host: EmbedderHost,
    memory: Memory,
): HostFunctionProvider = SyscallStat64Lstat64(memory, SyscallStatLstat64FunctionHandle.syscallLstat64(host))

internal class SyscallStat64Lstat64(
    private val memory: Memory,
    private val handle: SyscallStatLstat64FunctionHandle,
) : HostFunctionProvider {
    override val function: HostFunction = { args ->
        val result = handle.execute(
            memory,
            args[0].asWasmAddr(),
            args[1].asWasmAddr(),
        )
        listOf(Value.Number.I32(result))
    }
}
