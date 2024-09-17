/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.op.chown.ChownFd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.wasi.filesystem.common.Fd

public class SyscallFchown32FunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FCHOWN32, host) {
    public fun execute(@Fd fd: Int, owner: Int, group: Int): Int = host.fileSystem.execute(
        ChownFd,
        ChownFd(fd, owner, group),
    ).negativeErrnoCode()
}
