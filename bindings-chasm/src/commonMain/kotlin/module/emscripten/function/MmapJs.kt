/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asUInt
import at.released.weh.bindings.chasm.ext.asULong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.MmapJsFunctionHandle
import at.released.weh.host.include.sys.SysMmanMapFlags
import at.released.weh.host.include.sys.SysMmanProt
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class MmapJs(
    host: EmbedderHost,
) : HostFunction {
    private val handle = MmapJsFunctionHandle(host)

    @Suppress("MagicNumber")
    override fun invoke(args: List<Value>): List<Value> {
        val result: Int = handle.execute(
            args[0].asInt(),
            SysMmanProt(args[1].asUInt()),
            SysMmanMapFlags(args[2].asUInt()),
            Fd(args[3].asInt()),
            args[4].asULong(),
            args[5].asWasmAddr(),
            args[6].asWasmAddr(),
        )
        return listOf(Value.Number.I32(result))
    }
}
