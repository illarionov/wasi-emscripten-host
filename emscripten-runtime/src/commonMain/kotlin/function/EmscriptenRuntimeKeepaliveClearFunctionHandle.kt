/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.EMSCRIPTEN_RUNTIME_KEEPALIVE_CLEAR
import at.released.weh.host.EmbedderHost

public class EmscriptenRuntimeKeepaliveClearFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(EMSCRIPTEN_RUNTIME_KEEPALIVE_CLEAR, host) {
    public fun execute() {
        // abort / quit / noExitRuntime are not implemented yet, ignore
    }
}
