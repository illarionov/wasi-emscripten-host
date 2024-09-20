/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_FCNTL64
import at.released.weh.emcripten.runtime.FcntlHandler
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.memory.ReadOnlyMemory

public class SyscallFcntl64FunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_FCNTL64, host) {
    private val fcntlHandler = FcntlHandler(host.fileSystem)

    public fun execute(
        memory: ReadOnlyMemory,
        @IntFileDescriptor fd: FileDescriptor,
        cmd: Int,
        thirdArg: Int,
    ): Int {
        return fcntlHandler.invoke(memory, fd, cmd.toUInt(), thirdArg)
    }
}
