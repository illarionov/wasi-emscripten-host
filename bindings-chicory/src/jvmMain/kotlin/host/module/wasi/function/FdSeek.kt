/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chicory.host.module.wasi.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.wasi.WasiHostFunctionHandle
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.wasi.preview1.function.FdSeekFunctionHandle
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class FdSeek(
    host: EmbedderHost,
    private val memory: Memory,
) : WasiHostFunctionHandle {
    private val handle = FdSeekFunctionHandle(host)

    override fun apply(instance: Instance, vararg args: Value): Errno {
        val fd = Fd(args[0].asInt())
        val offset = args[1].asLong()
        val whenceInt = args[2].asInt()
        val pNewOffset: WasmPtr<Long> = args[3].asWasmAddr()
        return handle.execute(memory, fd, offset, whenceInt, pNewOffset)
    }
}
