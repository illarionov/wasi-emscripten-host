/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SETITIMER_JS
import at.released.weh.emcripten.runtime.include.sys.SysIntervalTimer
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno

public class SetitimerJsFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SETITIMER_JS, host) {
    public fun execute(
        @SysIntervalTimer which: Int,
        timeval: Double,
    ): Int {
        logger.v { "_setitimer_js($which, $timeval): Not implemented" }
        return -Errno.NOTSUP.code
    }
}
