/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.common.ext.encodedNullTerminatedStringLength
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.WasiArgsEnvironmentFunc.encodeEnvToWasi
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class EnvironSizesGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.ENVIRON_SIZES_GET, host) {
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
