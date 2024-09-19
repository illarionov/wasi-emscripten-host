/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.encodeToNullTerminatedBuffer
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize

public class SyscallGetcwdFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_GETCWD, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Byte::class) dst: WasmPtr,
        size: Int,
    ): Int {
        if (size == 0) {
            return -Errno.INVAL.code
        }
        return host.fileSystem.execute(GetCurrentWorkingDirectory, Unit)
            .fold(
                ifLeft = { -it.errno.code },
            ) { currentWorkingDirectory ->
                val pathBuffer = currentWorkingDirectory.encodeToNullTerminatedBuffer()
                if (size < pathBuffer.size) {
                    return@fold -Errno.RANGE.code
                }
                val pathSize = pathBuffer.size.toInt()
                memory.sinkWithMaxSize(dst, pathSize).use {
                    it.write(pathBuffer, pathSize.toLong())
                }
                pathSize
            }
    }
}
