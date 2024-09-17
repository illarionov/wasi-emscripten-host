/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.emscripten.function

import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.EmscriptenConsoleErrorFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class EmscriptenConsoleError(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<EmscriptenConsoleErrorFunctionHandle>(language, module, EmscriptenConsoleErrorFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        consoleError(
            memory(frame),
            frame.arguments.getArgAsWasmPtr(0),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun consoleError(
        memory: WasmMemory,
        @IntWasmPtr(Byte::class) messagePtr: WasmPtr,
    ): Unit = handle.execute(memory.toHostMemory(), messagePtr)
}
