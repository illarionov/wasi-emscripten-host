/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction

public class SyscallFtruncate64FunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_FTRUNCATE64, host) {
    public fun execute(@Fd fd: Int, length: Long): Int = host.fileSystem.execute(
        TruncateFd,
        TruncateFd(fd, length),
    ).fold(
        ifLeft = { -it.errno.code },
        ifRight = { Errno.SUCCESS.code },
    )
}
