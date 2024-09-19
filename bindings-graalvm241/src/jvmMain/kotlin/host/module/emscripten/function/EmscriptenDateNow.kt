/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.emcripten.runtime.function.EmscriptenDateNowFunctionHandle
import at.released.weh.host.EmbedderHost
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class EmscriptenDateNow(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<EmscriptenDateNowFunctionHandle>(language, module, EmscriptenDateNowFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        return emscriptenDateNow()
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun emscriptenDateNow(): Double = handle.execute()
}
