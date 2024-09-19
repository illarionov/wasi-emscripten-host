/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.emcripten.runtime.function.TzsetJsFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class TzsetJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<TzsetJsFunctionHandle>(language, module, TzsetJsFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        val args = frame.arguments
        tzsetJs(
            memory(frame),
            args.getArgAsWasmPtr(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsWasmPtr(2),
            args.getArgAsWasmPtr(3),
        )
    }

    @CompilerDirectives.TruffleBoundary
    @Suppress("MemberNameEqualsClassName", "MagicNumber")
    private fun tzsetJs(
        memory: WasmMemory,
        @IntWasmPtr(Int::class) timezone: WasmPtr,
        @IntWasmPtr(Int::class) daylight: WasmPtr,
        @IntWasmPtr(Byte::class) stdName: WasmPtr,
        @IntWasmPtr(Byte::class) dstName: WasmPtr,
    ) = handle.execute(memory.toHostMemory(), timezone, daylight, stdName, dstName)
}
