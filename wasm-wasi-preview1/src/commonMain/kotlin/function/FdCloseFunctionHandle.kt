/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiHostFunction
import at.released.weh.wasi.preview1.ext.toWasiErrno
import at.released.weh.wasi.preview1.type.Errno

public class FdCloseFunctionHandle(
    host: EmbedderHost,
) : WasiHostFunctionHandle(WasiHostFunction.FD_CLOSE, host) {
    public fun execute(
        @IntFileDescriptor fd: FileDescriptor,
    ): Errno = host.fileSystem.execute(CloseFd, CloseFd(fd))
        .fold(
            ifLeft = { it.errno.toWasiErrno() },
            ifRight = { Errno.SUCCESS },
        )
}
