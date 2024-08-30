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
import at.released.weh.bindings.chasm.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.MunapJsFunctionHandle
import at.released.weh.host.include.sys.SysMmanMapFlags
import at.released.weh.host.include.sys.SysMmanProt
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32

internal class MunmapJs(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle {
    private val handle = MunapJsFunctionHandle(host)

    @Suppress("MagicNumber")
    override fun invoke(args: List<ExecutionValue>): List<ExecutionValue> {
        val result: Int = handle.execute(
            args[0].asWasmAddr(),
            args[1].asInt(),
            SysMmanProt(args[2].asUInt()),
            SysMmanMapFlags(args[3].asUInt()),
            Fd(args[4].asInt()),
            args[5].asULong(),
        )
        return listOf(I32(result))
    }
}
