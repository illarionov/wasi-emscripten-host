/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.SyscallFtruncate64FunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class SyscallFtruncate64(
    host: EmbedderHost,
) : HostFunction {
    private val handle = SyscallFtruncate64FunctionHandle(host)

    override fun invoke(args: List<Value>): List<Value> {
        val result: Int = handle.execute(
            Fd(args[0].asInt()),
            args[1].asLong().toULong(),
        )
        return listOf(Value.Number.I32(result))
    }
}
