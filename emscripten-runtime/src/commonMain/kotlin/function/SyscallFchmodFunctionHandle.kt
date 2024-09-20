/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_FCHMOD
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.chmod.ChmodFd
import at.released.weh.host.EmbedderHost

public class SyscallFchmodFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_FCHMOD, host) {
    public fun execute(@IntFileDescriptor fd: FileDescriptor, @FileMode mode: Int): Int {
        return host.fileSystem.execute(ChmodFd, ChmodFd(fd, mode)).negativeErrnoCode()
    }
}
