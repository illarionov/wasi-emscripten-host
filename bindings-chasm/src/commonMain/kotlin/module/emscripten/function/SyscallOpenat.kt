/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asUInt
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readU32
import at.released.weh.host.emscripten.function.SyscallOpenatFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class SyscallOpenat(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : HostFunction {
    private val handle: SyscallOpenatFunctionHandle = SyscallOpenatFunctionHandle(host)

    override fun invoke(args: List<Value>): List<Value> {
        val mode = if (args.lastIndex == 3) {
            memory.readU32(args[3].asWasmAddr<Unit>())
        } else {
            0U
        }
        val fdOrErrno = handle.execute(
            memory,
            rawDirFd = args[0].asInt(),
            pathnamePtr = args[1].asWasmAddr(),
            rawFlags = args[2].asUInt(),
            rawMode = mode,
        )
        return listOf(Value.Number.I32(fdOrErrno))
    }
}