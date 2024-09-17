/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.wasi.function

import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.wasi.preview1.function.EnvironSizesGetFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class EnvironSizesGet(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<EnvironSizesGetFunctionHandle>(language, module, EnvironSizesGetFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        val args = frame.arguments
        return environSizesGet(
            memory(frame),
            args.getArgAsWasmPtr(0),
            args.getArgAsWasmPtr(1),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun environSizesGet(
        memory: WasmMemory,
        @IntWasmPtr(Int::class) environCountAddr: WasmPtr,
        @IntWasmPtr(Int::class) environSizeAddr: WasmPtr,
    ): Int = handle.execute(memory.toHostMemory(), environCountAddr, environSizeAddr).code
}
