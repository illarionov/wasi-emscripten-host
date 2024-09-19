/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.preview1.function.FdSeekFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import io.github.charlietap.chasm.embedding.shapes.Value

internal class FdSeek(
    host: EmbedderHost,
    private val memory: Memory,
) : WasiHostFunctionHandle {
    private val handle = FdSeekFunctionHandle(host)

    override operator fun invoke(args: List<Value>): Errno {
        val fd = args[0].asInt()
        val offset = args[1].asLong()
        val whenceInt = args[2].asInt()

        @IntWasmPtr(Long::class)
        val pNewOffset: WasmPtr = args[3].asWasmAddr()

        return handle.execute(memory, fd, offset, whenceInt, pNewOffset)
    }
}
