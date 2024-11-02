/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.fadvise.FadviseFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.type.Advice
import at.released.weh.wasi.preview1.type.Advice.DONTNEED
import at.released.weh.wasi.preview1.type.Advice.NOREUSE
import at.released.weh.wasi.preview1.type.Advice.NORMAL
import at.released.weh.wasi.preview1.type.Advice.RANDOM
import at.released.weh.wasi.preview1.type.Advice.SEQUENTIAL
import at.released.weh.wasi.preview1.type.Advice.WILLNEED
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.filesystem.op.fadvise.Advice as FileSystemAdvice

public class FdAdviseFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_ADVISE, host) {
    public fun execute(
        fd: FileDescriptor,
        offset: Long,
        length: Long,
        adviceCode: Byte,
    ): Errno {
        val advice = Advice.fromCode(adviceCode.toInt())?.toFileSystemAdvice() ?: return Errno.INVAL
        return host.fileSystem.execute(FadviseFd, FadviseFd(fd, offset, length, advice)).foldToErrno()
    }

    private fun Advice.toFileSystemAdvice(): FileSystemAdvice = when (this) {
        NORMAL -> FileSystemAdvice.NORMAL
        SEQUENTIAL -> FileSystemAdvice.SEQUENTIAL
        RANDOM -> FileSystemAdvice.RANDOM
        WILLNEED -> FileSystemAdvice.WILLNEED
        DONTNEED -> FileSystemAdvice.DONTNEED
        NOREUSE -> FileSystemAdvice.NOREUSE
    }
}
