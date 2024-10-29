/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.FILESTAT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.packTo
import at.released.weh.wasi.preview1.ext.toFilestat
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Filestat
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class FdFilestatGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_FILESTAT_GET, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Filestat::class) filestatAddr: WasmPtr,
    ): Errno {
        return host.fileSystem.execute(StatFd, StatFd(fd))
            .onRight { stat: StructStat ->
                memory.sinkWithMaxSize(filestatAddr, FILESTAT_PACKED_SIZE).buffered().use {
                    stat.toFilestat().packTo(it)
                }
            }.fold(
                ifLeft = { it.wasiErrno() },
                ifRight = { _ -> Errno.SUCCESS },
            )
    }
}
