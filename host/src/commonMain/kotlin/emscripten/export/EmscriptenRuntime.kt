/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.export

import at.released.weh.common.api.Logger
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.export.stack.EmscriptenStack
import at.released.weh.host.emscripten.export.stack.EmscriptenStackExports

public open class EmscriptenRuntime protected constructor(
    public val mainExports: EmscriptenMainExports,
    stackExports: EmscriptenStackExports,
    protected val memory: Memory,
    rootLogger: Logger,
) {
    private val logger: Logger = rootLogger.withTag("EmscriptenRuntime")
    public val stack: EmscriptenStack = EmscriptenStack(stackExports, logger)

    public open val isMultiThread: Boolean get() = false

    public open fun initMainThread(): Unit = initSingleThreadedMainThread()

    protected fun initSingleThreadedMainThread() {
        stack.stackCheckInit(memory)
        mainExports.__wasm_call_ctors.executeVoid()
        stack.checkStackCookie(memory)
    }

    public companion object {
        public fun emscriptenSingleThreadedRuntime(
            mainExports: EmscriptenMainExports,
            stackExports: EmscriptenStackExports,
            memory: Memory,
            logger: Logger,
        ): EmscriptenRuntime = EmscriptenRuntime(
            mainExports = mainExports,
            stackExports = stackExports,
            memory = memory,
            logger,
        )
    }
}
