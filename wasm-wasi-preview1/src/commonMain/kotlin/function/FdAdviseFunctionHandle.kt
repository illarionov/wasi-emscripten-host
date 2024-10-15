/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Advice
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.memory.Memory

public class FdAdviseFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.FD_ADVISE, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        fd: FileDescriptor,
        offset: Long,
        length: Long,
        adviceCode: Byte,
    ): Errno {
        Advice.fromCode(adviceCode.toInt()) ?: return Errno.INVAL
        // TODO
        return Errno.NOTSUP
    }
}
