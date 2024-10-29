/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.readCiovecs
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.Ciovec
import at.released.weh.wasi.preview1.type.CiovecArray
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Filesize
import at.released.weh.wasi.preview1.type.FilesizeType
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class FdPwriteFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_PWRITE, host) {
    public fun execute(
        memory: Memory,
        bulkWriter: WasiMemoryWriter,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Ciovec::class) pCiov: WasmPtr,
        cIovCnt: Int,
        @FilesizeType offset: Filesize,
        @IntWasmPtr(Size::class) expectedSize: WasmPtr,
    ): Errno {
        val cioVecs: CiovecArray = readCiovecs(memory, pCiov, cIovCnt)
        return bulkWriter.write(fd, ReadWriteStrategy.Position(offset), cioVecs)
            .onRight { writtenBytes ->
                memory.writeI32(expectedSize, writtenBytes.toInt())
            }.fold(
                ifLeft = FileSystemOperationError::wasiErrno,
                ifRight = { Errno.SUCCESS },
            )
    }
}
