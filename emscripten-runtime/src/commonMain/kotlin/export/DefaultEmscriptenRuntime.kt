/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.export

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.common.api.Logger
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStack
import at.released.weh.emcripten.runtime.export.stack.EmscriptenStackExports
import at.released.weh.wasm.core.memory.Memory

@InternalWasiEmscriptenHostApi
public open class DefaultEmscriptenRuntime protected constructor(
    public val mainExports: EmscriptenMainExports,
    stackExports: EmscriptenStackExports,
    protected val memory: Memory,
    rootLogger: Logger,
) : EmscriptenRuntime {
    private val logger: Logger = rootLogger.withTag("EmscriptenRuntime")
    public val stack: EmscriptenStack = EmscriptenStack(stackExports, logger)

    public override val isMultiThread: Boolean get() = false

    public override fun initMainThread(): Unit = initSingleThreadedMainThread()

    protected fun initSingleThreadedMainThread() {
        stack.stackCheckInit(memory)
        mainExports.__wasm_call_ctors.executeVoid()
        stack.checkStackCookie(memory)
    }

    public companion object {
        @InternalWasiEmscriptenHostApi
        public fun emscriptenSingleThreadedRuntime(
            mainExports: EmscriptenMainExports,
            stackExports: EmscriptenStackExports,
            memory: Memory,
            logger: Logger,
        ): DefaultEmscriptenRuntime = DefaultEmscriptenRuntime(
            mainExports = mainExports,
            stackExports = stackExports,
            memory = memory,
            logger,
        )
    }
}
