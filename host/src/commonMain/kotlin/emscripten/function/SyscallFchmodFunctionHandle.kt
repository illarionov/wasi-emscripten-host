/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.chmod.ChmodFd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.wasi.filesystem.common.Fd

public class SyscallFchmodFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FCHMOD, host) {
    public fun execute(@Fd fd: Int, @FileMode mode: Int): Int {
        return host.fileSystem.execute(ChmodFd, ChmodFd(fd, mode)).negativeErrnoCode()
    }
}
