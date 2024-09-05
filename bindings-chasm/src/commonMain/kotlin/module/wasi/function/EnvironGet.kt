/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.filesystem.model.Errno
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.wasi.preview1.function.EnvironGetFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.Value

internal class EnvironGet(
    host: EmbedderHost,
    private val memory: Memory,
) : WasiHostFunctionHandle {
    private val handle = EnvironGetFunctionHandle(host)

    override operator fun invoke(args: List<Value>): Errno {
        return handle.execute(memory, args[0].asWasmAddr(), args[1].asWasmAddr())
    }
}
