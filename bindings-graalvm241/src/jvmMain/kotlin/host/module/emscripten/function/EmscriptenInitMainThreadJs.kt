/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.BaseWasmNode
import at.released.weh.bindings.graalvm241.host.module.emscripten.function.EmscriptenInitMainThreadJs.InitMainThreadJsHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.emscripten.export.pthread.PthreadManager
import at.released.weh.host.include.StructPthread
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class EmscriptenInitMainThreadJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
    posixThreadRef: () -> PthreadManager,
) : BaseWasmNode<InitMainThreadJsHandle>(language, module, InitMainThreadJsHandle(host, posixThreadRef)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        val args = frame.arguments
        handle.execute(args.getArgAsWasmPtr(0))
    }

    class InitMainThreadJsHandle(
        host: EmbedderHost,
        private val posixThreadRef: () -> PthreadManager,
    ) : HostFunctionHandle(EmscriptenHostFunction.EMSCRIPTEN_INIT_MAIN_THREAD_JS, host) {
        @TruffleBoundary
        fun execute(@IntWasmPtr(StructPthread::class) ptr: WasmPtr) {
            posixThreadRef().initMainThreadJs(ptr)
        }
    }
}
