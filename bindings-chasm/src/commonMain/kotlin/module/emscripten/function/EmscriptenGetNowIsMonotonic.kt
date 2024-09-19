/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.module.emscripten.HostFunctionProvider
import at.released.weh.emcripten.runtime.function.EmscriptenGetNowIsMonotonicFunctionHandle
import at.released.weh.host.EmbedderHost
import io.github.charlietap.chasm.embedding.shapes.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class EmscriptenGetNowIsMonotonic(
    host: EmbedderHost,
) : HostFunctionProvider {
    private val handle = EmscriptenGetNowIsMonotonicFunctionHandle(host)
    override val function: HostFunction = {
        listOf(Value.Number.I32(handle.execute()))
    }
}
