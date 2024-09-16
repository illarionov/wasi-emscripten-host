/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asULong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.MunapJsFunctionHandle
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class MunmapJs(
    host: EmbedderHost,
) : HostFunctionProvider {
    private val handle = MunapJsFunctionHandle(host)

    @Suppress("MagicNumber")
    override val function: HostFunction = { args ->
        val result: Int = handle.execute(
            args[0].asWasmAddr(),
            args[1].asInt(),
            args[2].asInt(),
            args[3].asInt(),
            Fd(args[4].asInt()),
            args[5].asULong(),
        )
        listOf(Value.Number.I32(result))
    }
}
