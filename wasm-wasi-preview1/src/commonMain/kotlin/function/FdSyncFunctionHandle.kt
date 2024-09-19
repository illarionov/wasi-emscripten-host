/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasi.preview1.WasiHostFunction.FD_SYNC

public class FdSyncFunctionHandle(host: EmbedderHost) : WasiHostFunctionHandle(FD_SYNC, host) {
    public fun execute(@Fd fd: Int): Errno = host.fileSystem.execute(SyncFd, SyncFd(fd, true))
        .fold(
            ifLeft = SyncError::errno,
            ifRight = { Errno.SUCCESS },
        )
}
