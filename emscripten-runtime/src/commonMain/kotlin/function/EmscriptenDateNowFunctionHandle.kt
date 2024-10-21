/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.EMSCRIPTEN_DATE_NOW
import at.released.weh.host.EmbedderHost

public class EmscriptenDateNowFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(EMSCRIPTEN_DATE_NOW, host) {
    @Suppress("MagicNumber")
    public fun execute(): Double = host.clock.getCurrentTimeEpochNanoseconds().toDouble() / 1_000_000f
}
