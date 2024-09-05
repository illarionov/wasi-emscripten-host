/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.wasi.preview1.function.FdSyncSyscallFdatasyncFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class SyscallFdatasync(
    host: EmbedderHost,
) : HostFunction {
    val handle = FdSyncSyscallFdatasyncFunctionHandle.syscallFdatasync(host)

    override fun invoke(args: List<Value>): List<Value> {
        return listOf(Value.Number.I32(handle.execute(Fd(args[0].asInt())).code))
    }
}
