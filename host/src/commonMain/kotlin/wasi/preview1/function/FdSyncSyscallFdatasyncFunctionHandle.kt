/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunction
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.wasi.preview1.WasiHostFunction

public class FdSyncSyscallFdatasyncFunctionHandle private constructor(
    host: EmbedderHost,
    function: HostFunction,
    private val syncMetadata: Boolean,
) : HostFunctionHandle(function, host) {
    public fun execute(
        fd: Fd,
    ): Errno = host.fileSystem.execute(SyncFd, SyncFd(fd, syncMetadata))
        .fold(
            ifLeft = { it.errno },
            ifRight = { Errno.SUCCESS },
        )

    public companion object {
        public fun fdSync(
            host: EmbedderHost,
        ): FdSyncSyscallFdatasyncFunctionHandle = FdSyncSyscallFdatasyncFunctionHandle(
            host = host,
            function = WasiHostFunction.FD_SYNC,
            syncMetadata = true,
        )

        public fun syscallFdatasync(
            host: EmbedderHost,
        ): FdSyncSyscallFdatasyncFunctionHandle = FdSyncSyscallFdatasyncFunctionHandle(
            host = host,
            function = EmscriptenHostFunction.SYSCALL_FDATASYNC,
            syncMetadata = false,
        )
    }
}
