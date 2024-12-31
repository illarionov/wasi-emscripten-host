/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.memory.ChicoryMemoryProvider
import at.released.weh.emcripten.runtime.function.SyscallStatLstat64FunctionHandle
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.WasmFunctionHandle

internal fun syscallStat64(
    host: EmbedderHost,
    memoryProvider: ChicoryMemoryProvider,
): WasmFunctionHandle =
    SyscallStat64Lstat64(memoryProvider, SyscallStatLstat64FunctionHandle.syscallStat64(host))

internal fun syscallLstat64(
    host: EmbedderHost,
    memoryProvider: ChicoryMemoryProvider,
): WasmFunctionHandle =
    SyscallStat64Lstat64(memoryProvider, SyscallStatLstat64FunctionHandle.syscallLstat64(host))

internal class SyscallStat64Lstat64(
    private val memoryProvider: ChicoryMemoryProvider,
    private val handle: SyscallStatLstat64FunctionHandle,
) : WasmFunctionHandle {
    override fun apply(instance: Instance, vararg args: Long): LongArray {
        val result = handle.execute(
            memoryProvider.get(instance),
            args[0].asWasmAddr(),
            args[1].asWasmAddr(),
        )
        return LongArray(1) { result.toLong() }
    }
}
