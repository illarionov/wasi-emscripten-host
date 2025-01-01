/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MemberNameEqualsClassName")

package at.released.weh.bindings.graalvm241.wasip1

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.wasip1.ProcExit.ProcExitFunctionHandle
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.function.WasiPreview1HostFunctionHandle
import at.released.weh.wasi.preview1.type.Exitcode
import at.released.weh.wasi.preview1.type.ExitcodeType
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.Node
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.exception.WasmExit

internal class ProcExit(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasiWasmNode<ProcExitFunctionHandle>(language, module, ProcExitFunctionHandle(host)) {
    override fun executeWithContext(
        frame: VirtualFrame,
        context: WasmContext,
        instance: WasmInstance,
    ): Any {
        val args = frame.arguments
        procExit(args.getArgAsInt(0))
    }

    @TruffleBoundary
    private fun procExit(rval: Int): Nothing = handle.execute(this, rval)

    internal class ProcExitFunctionHandle(
        host: EmbedderHost,
    ) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PROC_EXIT, host) {
        fun execute(location: Node, @ExitcodeType exitCode: Exitcode): Nothing = throw WasmExit(location, exitCode)
    }
}
