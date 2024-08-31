/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chicory.host.module.emscripten.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readU32
import at.released.weh.host.emscripten.function.SyscallOpenatFunctionHandle
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class SyscallOpenat(
    host: EmbedderHost,
    private val memory: ReadOnlyMemory,
) : EmscriptenHostFunctionHandle {
    private val handle: SyscallOpenatFunctionHandle = SyscallOpenatFunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Value): Value? {
        val mode = if (args.lastIndex == 3) {
            memory.readU32(args[3].asWasmAddr<Unit>())
        } else {
            0U
        }
        val fdOrErrno = handle.execute(
            memory,
            rawDirFd = args[0].asInt(),
            pathnamePtr = args[1].asWasmAddr(),
            rawFlags = args[2].asInt().toUInt(),
            rawMode = mode,
        )
        return Value.i32(fdOrErrno.toLong())
    }
}
