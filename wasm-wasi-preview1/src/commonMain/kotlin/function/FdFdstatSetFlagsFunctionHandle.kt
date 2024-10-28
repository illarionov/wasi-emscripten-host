/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.setfdflags.SetFdFlags
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.WasiFdFlagsMapper
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Fdflags

public class FdFdstatSetFlagsFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_FDSTAT_SET_FLAGS, host) {
    public fun execute(
        @IntFileDescriptor fd: FileDescriptor,
        fdflags: Fdflags,
    ): Errno {
        val fsFlags = WasiFdFlagsMapper.getFsFdlags(fdflags)
        return host.fileSystem.execute(SetFdFlags, SetFdFlags(fd, fsFlags))
            .fold(
                ifLeft = SetFdFlagsError::wasiErrno,
                ifRight = { Errno.SUCCESS },
            )
    }
}
