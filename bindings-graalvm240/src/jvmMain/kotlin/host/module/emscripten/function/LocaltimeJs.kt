/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.emscripten.function

import at.released.weh.bindings.graalvm240.ext.getArgAsLong
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.emscripten.function.LocaltimeJsFunctionHandle
import at.released.weh.host.include.StructTm
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class LocaltimeJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<LocaltimeJsFunctionHandle>(language, module, LocaltimeJsFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        val args = frame.arguments
        localtimeJs(
            memory(frame),
            args.getArgAsLong(0),
            args.getArgAsWasmPtr(1),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun localtimeJs(
        memory: WasmMemory,
        time: Long,
        timePtr: WasmPtr<StructTm>,
    ) = handle.execute(memory.toHostMemory(), time, timePtr)
}
