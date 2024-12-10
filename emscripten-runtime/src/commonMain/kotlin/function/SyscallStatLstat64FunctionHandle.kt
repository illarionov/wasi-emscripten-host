/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import arrow.core.getOrElse
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_LSTAT64
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_STAT64
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.emcripten.runtime.include.sys.STRUCT_SIZE_PACKED_SIZE
import at.released.weh.emcripten.runtime.include.sys.packTo
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.HostFunction
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.readNullTerminatedString
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class SyscallStatLstat64FunctionHandle private constructor(
    host: EmbedderHost,
    private val followSymlinks: Boolean = false,
    function: HostFunction,
) : EmscriptenHostFunctionHandle(function, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @IntWasmPtr(StructStat::class) dstAddr: WasmPtr,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        val virtualPath = VirtualPath.create(path).getOrElse { _ -> return -Errno.INVAL.code }

        return host.fileSystem.execute(
            Stat,
            Stat(
                path = virtualPath,
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
            SYSCALL_LSTAT64,
        )

        public fun syscallStat64(
            host: EmbedderHost,
        ): SyscallStatLstat64FunctionHandle = SyscallStatLstat64FunctionHandle(
            host,
            true,
            SYSCALL_STAT64,
        )
    }
}
