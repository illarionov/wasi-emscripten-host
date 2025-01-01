/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenThreadMailboxAwait.EmscriptenThreadMailboxAwaitHandle
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.EMSCRIPTEN_INIT_MAIN_THREAD_JS
import at.released.weh.emcripten.runtime.export.pthread.PthreadManager
import at.released.weh.emcripten.runtime.function.EmscriptenHostFunctionHandle
import at.released.weh.host.EmbedderHost
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class EmscriptenThreadMailboxAwait(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
    @Suppress("UnusedPrivateProperty")
    private val posixThreadRef: () -> PthreadManager,
) : BaseEmscriptenWasmNode<EmscriptenThreadMailboxAwaitHandle>(
    language,
    module,
    EmscriptenThreadMailboxAwaitHandle(host),
) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        handle.execute(frame.arguments.getArgAsInt(0))
    }

    class EmscriptenThreadMailboxAwaitHandle(
        host: EmbedderHost,
    ) : EmscriptenHostFunctionHandle(EMSCRIPTEN_INIT_MAIN_THREAD_JS, host) {
        @TruffleBoundary
        fun execute(threadPtr: Int) {
            logger.v { "_emscripten_thread_mailbox_await($threadPtr): skip, not implemented" }
        }
    }
}
