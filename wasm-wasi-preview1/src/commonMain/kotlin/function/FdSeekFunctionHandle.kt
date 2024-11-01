/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.WhenceMapper
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Filedelta
import at.released.weh.wasi.preview1.type.FiledeltaType
import at.released.weh.wasi.preview1.type.Filesize
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class FdSeekFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_SEEK, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @FiledeltaType offset: Filedelta,
        whenceCode: Byte,
        @IntWasmPtr(Filesize::class) pNewOffset: WasmPtr,
    ): Errno {
        val whence = WhenceMapper.fromWasiCodeOrNull(whenceCode.toInt()) ?: return Errno.INVAL
        return host.fileSystem.execute(
            SeekFd,
            SeekFd(fd = fd, fileDelta = offset, whence = whence),
        ).onRight { newPosition ->
            memory.writeI64(pNewOffset, newPosition)
        }.foldToErrno()
    }
}
