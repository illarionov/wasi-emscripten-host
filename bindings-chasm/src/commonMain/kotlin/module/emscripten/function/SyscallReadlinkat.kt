/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.function.SyscallReadlinkatFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class SyscallReadlinkat(
    host: EmbedderHost,
    private val memory: Memory,
) : HostFunction {
    private val handle = SyscallReadlinkatFunctionHandle(host)

    override fun invoke(args: List<Value>): List<Value> {
        val sizeOrErrno = handle.execute(
            memory,
            rawDirFd = args[0].asInt(),
            pathnamePtr = args[1].asWasmAddr(),
            buf = args[2].asWasmAddr(),
            bufSize = args[3].asInt(),
        )
        return listOf(Value.Number.I32(sizeOrErrno))
    }
}
