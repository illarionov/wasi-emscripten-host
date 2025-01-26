/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.emcripten.runtime.function.SetitimerJsFunctionHandle
import at.released.weh.emcripten.runtime.include.sys.SysIntervalTimer
import at.released.weh.host.EmbedderHost
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmArguments
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class SetittimerJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<SetitimerJsFunctionHandle>(language, module, SetitimerJsFunctionHandle(host)) {
    @Suppress("MagicNumber")
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Int {
        val args = frame.arguments
        return settimerJs(
            args.getArgAsInt(0),
            WasmArguments.getArgument(args, 1) as Double,
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun settimerJs(
        @SysIntervalTimer which: Int,
        timeval: Double,
    ): Int = handle.execute(which, timeval)
}
