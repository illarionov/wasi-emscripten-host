/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.emcripten.runtime.function.EmscriptenDateNowFunctionHandle
import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.runtime.value.NumberValue

internal class EmscriptenDateNow(host: EmbedderHost) : HostFunctionProvider {
    private val handle = EmscriptenDateNowFunctionHandle(host)
    override val function: HostFunction = {
        listOf(NumberValue.F64(handle.execute()))
    }
}
