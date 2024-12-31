/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.memory.ChicoryMemoryProvider
import at.released.weh.emcripten.runtime.function.SyscallOpenatFunctionHandle
import at.released.weh.host.EmbedderHost
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.WasmFunctionHandle

internal class SyscallOpenat(
    host: EmbedderHost,
    private val memoryProvider: ChicoryMemoryProvider,
) : WasmFunctionHandle {
    private val handle: SyscallOpenatFunctionHandle = SyscallOpenatFunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Long): LongArray {
        val memory = memoryProvider.get(instance)
        val mode = if (args.lastIndex == 3) {
            memory.readI32(args[3].asWasmAddr())
        } else {
            0
        }
        val fdOrErrno = handle.execute(
            memory,
            rawDirFd = args[0].toInt(),
            pathnamePtr = args[1].asWasmAddr(),
            rawFlags = args[2].toInt(),
            rawMode = mode,
        )
        return LongArray(1) { fdOrErrno.toLong() }
    }
}
