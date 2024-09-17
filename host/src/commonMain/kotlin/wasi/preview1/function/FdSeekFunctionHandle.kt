/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.wasi.preview1.WasiHostFunction

public class FdSeekFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(WasiHostFunction.FD_SEEK, host) {
    public fun execute(
        memory: Memory,
        @Fd fd: Int,
        offset: Long,
        whenceInt: Int,
        @IntWasmPtr(Long::class) pNewOffset: WasmPtr,
    ): Errno {
        val whence = Whence.fromIdOrNull(whenceInt) ?: return Errno.INVAL
        return host.fileSystem.execute(
            SeekFd,
            SeekFd(fd = fd, fileDelta = offset, whence = whence),
        ).onRight { newPosition ->
            memory.writeI64(pNewOffset, newPosition)
        }.fold(
            ifLeft = SeekError::errno,
            ifRight = { _ -> Errno.SUCCESS },
        )
    }
}
