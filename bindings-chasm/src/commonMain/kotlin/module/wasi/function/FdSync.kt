/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.wasi.preview1.function.FdSyncSyscallFdatasyncFunctionHandle
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue

internal class FdSync(
    host: EmbedderHost,
) : WasiHostFunctionHandle {
    private val handle = FdSyncSyscallFdatasyncFunctionHandle.fdSync(host)

    override fun invoke(args: List<ExecutionValue>): Errno {
        val fd = Fd(args[0].asInt())
        return handle.execute(fd)
    }
}
