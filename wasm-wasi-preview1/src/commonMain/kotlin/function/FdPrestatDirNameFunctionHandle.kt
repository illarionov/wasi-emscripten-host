/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.PrestatError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.prestat.PrestatResult
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.encodeToBuffer
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize

public class FdPrestatDirNameFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_PRESTAT_DIR_NAME, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Byte::class) dstPath: WasmPtr,
        dstPathLen: Int,
    ): Errno {
        return host.fileSystem.execute(PrestatFd, PrestatFd(fd))
            .mapLeft(PrestatError::wasiErrno)
            .flatMap { prestatResult: PrestatResult ->
                val bytes = prestatResult.path.encodeToBuffer()
                if (bytes.size > dstPathLen) {
                    return@flatMap Errno.NAMETOOLONG.left()
                }
                memory.sinkWithMaxSize(dstPath, bytes.size.toInt()).use {
                    it.write(bytes, bytes.size)
                }
                Errno.SUCCESS.right()
            }.getOrElse { it }
    }
}
