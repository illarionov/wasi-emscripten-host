/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.prestat.PrestatResult
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.PRESTAT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.encodedLength
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.packTo
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Preopentype
import at.released.weh.wasi.preview1.type.Prestat
import at.released.weh.wasi.preview1.type.PrestatDir
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class FdPrestatGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_PRESTAT_GET, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Prestat::class) dstAddr: WasmPtr,
    ): Errno {
        return host.fileSystem.execute(PrestatFd, PrestatFd(fd))
            .onRight { prestatResult: PrestatResult ->
                memory.sinkWithMaxSize(dstAddr, PRESTAT_PACKED_SIZE).buffered().use {
                    PrestatDir(
                        tag = Preopentype.DIR,
                        prNameLen = prestatResult.path.encodedLength(),
                    ).packTo(it)
                }
            }.foldToErrno()
    }
}
