/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.readIovecs
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasi.preview1.type.IovecArray
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class FdPreadFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_PREAD, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        bulkReader: WasiMemoryReader,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Iovec::class) pIov: WasmPtr,
        iovCnt: Int,
        offset: Long, // TODO: offset
        @IntWasmPtr(Int::class) pNum: WasmPtr,
    ): Errno {
        val ioVecs: IovecArray = readIovecs(memory, pIov, iovCnt)
        return bulkReader.read(fd, DO_NOT_CHANGE_POSITION, ioVecs)
            .onRight { readBytes -> memory.writeI32(pNum, readBytes.toInt()) }
            .fold(
                ifLeft = FileSystemOperationError::wasiErrno,
                ifRight = { Errno.SUCCESS },
            )
    }
}
