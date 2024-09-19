/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.ext.encodedNullTerminatedStringLength
import at.released.weh.host.wasi.preview1.ext.WasiEnvironmentFunc.encodeEnvToWasi
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class EnvironSizesGetFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(WasiHostFunction.ENVIRON_SIZES_GET, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Int::class) environCountAddr: WasmPtr,
        @IntWasmPtr(Int::class) environSizeAddr: WasmPtr,
    ): Errno {
        val env = host.systemEnvProvider.getSystemEnv()
        val count = env.size
        val dataLength = env.entries.sumOf { it.encodeEnvToWasi().encodedNullTerminatedStringLength() }
        memory.writeI32(
            addr = environCountAddr,
            data = count,
        )
        memory.writeI32(
            addr = environSizeAddr,
            data = dataLength,
        )
        return Errno.SUCCESS
    }
}
