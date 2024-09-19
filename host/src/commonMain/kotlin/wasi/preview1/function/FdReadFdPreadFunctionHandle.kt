/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.WasiMemoryReader
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasi.preview1.type.IovecArray
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasm.core.HostFunction
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readPtr

public class FdReadFdPreadFunctionHandle private constructor(
    host: EmbedderHost,
    function: HostFunction,
    private val strategy: ReadWriteStrategy,
) : HostFunctionHandle(function, host) {
    public fun execute(
        memory: Memory,
        bulkReader: WasiMemoryReader,
        @Fd fd: Int,
        @IntWasmPtr(Iovec::class) pIov: WasmPtr,
        iovCnt: Int,
        @IntWasmPtr(Iovec::class) pNum: WasmPtr,
    ): Errno {
        val ioVecs: IovecArray = readIovecs(memory, pIov, iovCnt)
        return bulkReader.read(fd, strategy, ioVecs.iovecList)
            .onRight { readBytes -> memory.writeI32(pNum, readBytes.toInt()) }
            .fold(
                ifLeft = FileSystemOperationError::errno,
                ifRight = { Errno.SUCCESS },
            )
    }

    public companion object {
        public fun fdRead(
            host: EmbedderHost,
        ): FdReadFdPreadFunctionHandle = FdReadFdPreadFunctionHandle(
            host,
            WasiHostFunction.FD_READ,
            ReadWriteStrategy.CHANGE_POSITION,
        )

        public fun fdPread(
            host: EmbedderHost,
        ): FdReadFdPreadFunctionHandle = FdReadFdPreadFunctionHandle(
            host,
            WasiHostFunction.FD_PREAD,
            ReadWriteStrategy.DO_NOT_CHANGE_POSITION,
        )

        private fun readIovecs(
            memory: ReadOnlyMemory,
            @IntWasmPtr(Iovec::class) pIov: WasmPtr,
            iovCnt: Int,
        ): IovecArray {
            val iovecs = MutableList(iovCnt) { idx ->
                val pIovec: WasmPtr = pIov + 8 * idx
                Iovec(
                    buf = memory.readPtr(pIovec),
                    bufLen = Size(memory.readI32(pIovec + 4)),
                )
            }
            return IovecArray(iovecs)
        }
    }
}
