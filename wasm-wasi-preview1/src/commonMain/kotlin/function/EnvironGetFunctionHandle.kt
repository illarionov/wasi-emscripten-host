/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasi.preview1.ext.WasiEnvironmentFunc.encodeEnvToWasi
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.writeNullTerminatedString

public class EnvironGetFunctionHandle(
    host: EmbedderHost,
) : WasiHostFunctionHandle(WasiHostFunction.ENVIRON_GET, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Int::class) environPAddr: WasmPtr,
        @IntWasmPtr(Int::class) environBufAddr: WasmPtr,
    ): Errno {
        var pp = environPAddr
        var bufP = environBufAddr
        host.systemEnvProvider.getSystemEnv()
            .entries
            .map { it.encodeEnvToWasi() }
            .forEach { envString ->
                memory.writeI32(pp, bufP)
                pp += 4
                bufP += memory.writeNullTerminatedString(bufP, envString)
            }
        return Errno.SUCCESS
    }
}
