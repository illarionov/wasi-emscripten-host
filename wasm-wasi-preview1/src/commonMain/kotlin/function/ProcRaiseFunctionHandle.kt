/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Signal
import at.released.weh.wasm.core.memory.Memory

public class ProcRaiseFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PROC_RAISE, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        signalCode: Byte,
    ): Errno {
        Signal.fromCode(signalCode.toInt()) ?: return Errno.INVAL
        return Errno.NOTSUP
    }
}
