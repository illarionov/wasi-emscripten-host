/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileSystemErrno.Companion.wasiPreview1Code
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno

public class SyscallFtruncate64FunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(EmscriptenHostFunction.SYSCALL_FTRUNCATE64, host) {
    public fun execute(@IntFileDescriptor fd: FileDescriptor, length: Long): Int = host.fileSystem.execute(
        TruncateFd,
        TruncateFd(fd, length),
    ).fold(
        ifLeft = { -it.errno.wasiPreview1Code },
        ifRight = { Errno.SUCCESS.code },
    )
}
