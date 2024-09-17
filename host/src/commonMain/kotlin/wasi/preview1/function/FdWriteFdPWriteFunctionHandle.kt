/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunction
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.WasiMemoryWriter
import at.released.weh.host.base.memory.readPtr
import at.released.weh.host.wasi.preview1.WasiHostFunction
import at.released.weh.host.wasi.preview1.type.CioVec
import at.released.weh.host.wasi.preview1.type.CiovecArray
import at.released.weh.host.wasi.preview1.type.Size
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public class FdWriteFdPWriteFunctionHandle private constructor(
    host: EmbedderHost,
    function: HostFunction,
    private val strategy: ReadWriteStrategy,
) : HostFunctionHandle(function, host) {
    public fun execute(
        memory: Memory,
        bulkWriter: WasiMemoryWriter,
        @Fd fd: Int,
        @IntWasmPtr(CioVec::class) pCiov: WasmPtr,
        cIovCnt: Int,
        @IntWasmPtr(Int::class) pNum: WasmPtr,
    ): Errno {
        val cioVecs: CiovecArray = readCiovecs(memory, pCiov, cIovCnt)
        return bulkWriter.write(fd, strategy, cioVecs.ciovecList)
            .onRight { writtenBytes ->
                memory.writeI32(pNum, writtenBytes.toInt())
            }.fold(
                ifLeft = FileSystemOperationError::errno,
                ifRight = { Errno.SUCCESS },
            )
    }

    public companion object {
        public fun fdWrite(
            host: EmbedderHost,
        ): FdWriteFdPWriteFunctionHandle = FdWriteFdPWriteFunctionHandle(
            host,
            WasiHostFunction.FD_WRITE,
            ReadWriteStrategy.CHANGE_POSITION,
        )

        public fun fdPwrite(
            host: EmbedderHost,
        ): FdWriteFdPWriteFunctionHandle = FdWriteFdPWriteFunctionHandle(
            host,
            WasiHostFunction.FD_PWRITE,
            ReadWriteStrategy.DO_NOT_CHANGE_POSITION,
        )

        private fun readCiovecs(
            memory: ReadOnlyMemory,
            @IntWasmPtr(CioVec::class) pCiov: WasmPtr,
            ciovCnt: Int,
        ): CiovecArray {
            val iovecs = MutableList(ciovCnt) { idx ->
                val pCiovec: WasmPtr = pCiov + 8 * idx
                CioVec(
                    buf = memory.readPtr(pCiovec),
                    bufLen = Size(memory.readI32(pCiovec + 4)),
                )
            }
            return CiovecArray(iovecs)
        }
    }
}
