/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.emscripten.FcntlHandler
import at.released.weh.wasi.filesystem.common.Fd

public class SyscallFcntl64FunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FCNTL64, host) {
    private val fcntlHandler = FcntlHandler(host.fileSystem)

    public fun execute(
        memory: ReadOnlyMemory,
        @Fd fd: Int,
        cmd: Int,
        thirdArg: Int,
    ): Int {
        return fcntlHandler.invoke(memory, fd, cmd.toUInt(), thirdArg)
    }
}
