/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.common.ext.encodedNullTerminatedStringLength
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.WasiArgsEnvironmentFunc.cleanupProgramArgument
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class ArgsSizesGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.ARGS_SIZES_GET, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Int::class) argvAddr: WasmPtr,
        @IntWasmPtr(Int::class) argvBufSizeAddr: WasmPtr,
    ): Errno {
        val args = host.commandArgsProvider.getCommandArgs()
        val count = args.size
        val dataLength = args.sumOf { cleanupProgramArgument(it).encodedNullTerminatedStringLength() }

        memory.writeI32(addr = argvAddr, data = count)
        memory.writeI32(addr = argvBufSizeAddr, data = dataLength)
        return Errno.SUCCESS
    }
}
