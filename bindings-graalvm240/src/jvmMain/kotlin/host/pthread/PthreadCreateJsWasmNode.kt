/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.pthread

import at.released.weh.bindings.graalvm240.ext.getArgAsInt
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.bindings.graalvm240.host.pthread.PthreadCreateJsWasmNode.PthreadCreateJsFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.include.StructPthread
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class PthreadCreateJsWasmNode(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
    posixThreadRef: () -> GraalvmPthreadManager,
) : BaseWasmNode<PthreadCreateJsFunctionHandle>(
    language,
    module,
    PthreadCreateJsFunctionHandle(host, posixThreadRef),
) {
    @Suppress("MagicNumber")
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Int {
        val args = frame.arguments
        return handle.execute(
            args.getArgAsWasmPtr(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsInt(2),
            args.getArgAsWasmPtr(3),
        )
    }

    class PthreadCreateJsFunctionHandle(
        host: EmbedderHost,
        private val pThreadManagerRef: () -> GraalvmPthreadManager,
    ) : HostFunctionHandle(EmscriptenHostFunction.PTHREAD_CREATE_JS, host) {
        @TruffleBoundary
        fun execute(
            @IntWasmPtr(StructPthread::class) pthreadPtr: WasmPtr,
            @IntWasmPtr(Int::class) attr: WasmPtr,
            startRoutine: Int,
            @IntWasmPtr arg: WasmPtr,
        ): Int {
            logger.v { "pthread_create_js(pthreadPtr=$pthreadPtr, attr=$attr, startRoutine=$startRoutine, arg=$arg)" }
            return pThreadManagerRef().spawnThread(pthreadPtr, attr, startRoutine, arg)
        }
    }
}
