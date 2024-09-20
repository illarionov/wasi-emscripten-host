/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasi.preview1.ext.WhenceMapper
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class FdSeekFunctionHandle(
    host: EmbedderHost,
) : WasiHostFunctionHandle(WasiHostFunction.FD_SEEK, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        offset: Long,
        whenceInt: Int,
        @IntWasmPtr(Long::class) pNewOffset: WasmPtr,
    ): Errno {
        val whence = WhenceMapper.fromWasiCodeOrNull(whenceInt) ?: return Errno.INVAL
        return host.fileSystem.execute(
            SeekFd,
            SeekFd(fd = fd, fileDelta = offset, whence = whence),
        ).onRight { newPosition ->
            memory.writeI64(pNewOffset, newPosition)
        }.fold(
            ifLeft = SeekError::wasiErrno,
            ifRight = { _ -> Errno.SUCCESS },
        )
    }
}
