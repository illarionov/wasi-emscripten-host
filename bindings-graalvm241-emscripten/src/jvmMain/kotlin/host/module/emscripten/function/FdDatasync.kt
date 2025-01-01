/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName", "CommentWrapping")

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.emcripten.runtime.function.FdDatasyncFunctionHandle
import at.released.weh.host.EmbedderHost
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class FdDatasync(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<FdDatasyncFunctionHandle>(language, module, FdDatasyncFunctionHandle(host)) {
    override fun executeWithContext(
        frame: VirtualFrame,
        context: WasmContext,
        instance: WasmInstance,
    ): Any {
        val args = frame.arguments
        return fdDatasync(
            args.getArgAsInt(0), /* fd */
        )
    }

    @TruffleBoundary
    private fun fdDatasync(fd: Int): Int = handle.execute(fd).code
}
