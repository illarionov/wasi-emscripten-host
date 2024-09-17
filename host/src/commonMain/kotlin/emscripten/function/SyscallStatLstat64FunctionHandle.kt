/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunction
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.base.memory.sinkWithMaxSize
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.host.include.sys.STRUCT_SIZE_PACKED_SIZE
import at.released.weh.host.include.sys.packTo
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.buffered

public class SyscallStatLstat64FunctionHandle private constructor(
    host: EmbedderHost,
    private val followSymlinks: Boolean = false,
    function: HostFunction,
) : HostFunctionHandle(function, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @IntWasmPtr(StructStat::class) dstAddr: WasmPtr,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        return host.fileSystem.execute(
            Stat,
            Stat(
                path = path,
                baseDirectory = CurrentWorkingDirectory,
                followSymlinks = followSymlinks,
            ),
        ).map { stat: StructStat ->
            memory.sinkWithMaxSize(dstAddr, STRUCT_SIZE_PACKED_SIZE).buffered().use {
                stat.packTo(it)
            }
        }.negativeErrnoCode()
    }

    public companion object {
        public fun syscallLstat64(
            host: EmbedderHost,
        ): SyscallStatLstat64FunctionHandle = SyscallStatLstat64FunctionHandle(
            host,
            false,
            EmscriptenHostFunction.SYSCALL_LSTAT64,
        )

        public fun syscallStat64(
            host: EmbedderHost,
        ): SyscallStatLstat64FunctionHandle = SyscallStatLstat64FunctionHandle(
            host,
            true,
            EmscriptenHostFunction.SYSCALL_STAT64,
        )
    }
}
