/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.EmscriptenAsmConstIntFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class EmscriptenAsmConstInt(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<EmscriptenAsmConstIntFunctionHandle>(language, module, EmscriptenAsmConstIntFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance) {
        val args = frame.arguments
        asmConstAsyncOnMainThread(
            args.getArgAsWasmPtr(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsWasmPtr(2),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun asmConstAsyncOnMainThread(
        @IntWasmPtr(Byte::class) emAsmAddr: WasmPtr,
        @IntWasmPtr(Byte::class) sigPtr: WasmPtr,
        @IntWasmPtr(Byte::class) argbuf: WasmPtr,
    ) {
        handle.execute(emAsmAddr, sigPtr, argbuf)
    }
}
