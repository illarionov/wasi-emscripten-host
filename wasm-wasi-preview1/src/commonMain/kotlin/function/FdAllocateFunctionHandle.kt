/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.fallocate.FallocateFd
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.type.Errno

public class FdAllocateFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_ALLOCATE, host) {
    public fun execute(
        @IntFileDescriptor fd: FileDescriptor,
        offset: Long,
        len: Long,
    ): Errno {
        return host.fileSystem.execute(FallocateFd, FallocateFd(fd, offset, len)).foldToErrno()
    }
}
