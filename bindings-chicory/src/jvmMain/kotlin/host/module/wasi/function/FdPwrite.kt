/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.wasi.function

import at.released.weh.bindings.chicory.ext.asWasmAddr
import at.released.weh.bindings.chicory.host.module.wasi.WasiHostFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdPwriteFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.memory.Memory
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal class FdPwrite(
    host: EmbedderHost,
    private val memory: Memory,
    private val wasiMemoryWriter: WasiMemoryWriter,
) : WasiHostFunctionHandle {
    private val handle = FdPwriteFunctionHandle(host)
    override fun apply(instance: Instance, vararg args: Value): Errno {
        return handle.execute(
            memory,
            wasiMemoryWriter,
            args[0].asInt(),
            args[1].asWasmAddr(),
            args[2].asInt(),
            args[3].asLong(),
            args[4].asWasmAddr(),
        )
    }
}
