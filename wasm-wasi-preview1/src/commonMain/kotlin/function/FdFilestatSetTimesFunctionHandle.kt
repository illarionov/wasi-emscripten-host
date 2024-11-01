/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.getRequestedAtimeMtime
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Fstflags
import at.released.weh.wasi.preview1.type.FstflagsType
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasi.preview1.type.TimestampType

public class FdFilestatSetTimesFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_FILESTAT_SET_TIMES, host) {
    public fun execute(
        @IntFileDescriptor fd: FileDescriptor,
        @TimestampType atime: Timestamp,
        @TimestampType mtime: Timestamp,
        @FstflagsType fstflags: Fstflags,
    ): Errno {
        val reqTimestamps = getRequestedAtimeMtime(host.clock, atime, mtime, fstflags).getOrElse { return it }
        return host.fileSystem.execute(
            SetTimestampFd,
            SetTimestampFd(fd, reqTimestamps.atimeNanoseconds, reqTimestamps.mtimeNanoseconds),
        ).foldToErrno()
    }
}
