/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_FDATASYNC
import at.released.weh.emcripten.runtime.ext.wasiErrno
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasi.preview1.type.Errno

public class FdDatasyncFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_FDATASYNC, host) {
    public fun execute(
        @Fd fd: Int,
    ): Errno = host.fileSystem.execute(SyncFd, SyncFd(fd, false))
        .fold(
            ifLeft = SyncError::wasiErrno,
            ifRight = { Errno.SUCCESS },
        )
}
