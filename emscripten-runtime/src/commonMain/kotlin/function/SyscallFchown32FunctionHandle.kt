/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_FCHOWN32
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.chown.ChownFd
import at.released.weh.host.EmbedderHost

public class SyscallFchown32FunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_FCHOWN32, host) {
    public fun execute(@IntFileDescriptor fd: FileDescriptor, owner: Int, group: Int): Int = host.fileSystem.execute(
        ChownFd,
        ChownFd(fd, owner, group),
    ).negativeErrnoCode()
}
