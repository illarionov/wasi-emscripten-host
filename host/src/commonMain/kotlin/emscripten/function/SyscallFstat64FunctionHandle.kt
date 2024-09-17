/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.sinkWithMaxSize
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.host.include.sys.STRUCT_SIZE_PACKED_SIZE
import at.released.weh.host.include.sys.packTo
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.buffered

public class SyscallFstat64FunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FSTAT64, host) {
    public fun execute(
        memory: Memory,
        @Fd fd: Int,
        @IntWasmPtr(StructStat::class) dst: WasmPtr,
    ): Int = host.fileSystem.execute(StatFd, StatFd(fd))
        .map { stat: StructStat ->
            memory.sinkWithMaxSize(dst, STRUCT_SIZE_PACKED_SIZE).buffered().use {
                stat.packTo(it)
            }
        }.negativeErrnoCode()
}
