/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.wasi.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.wasi.BaseWasiWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdFdstatGetFunctionHandle
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class FdStatGet(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasiWasmNode<FdFdstatGetFunctionHandle>(language, module, FdFdstatGetFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        return fdStatGet(
            memory(frame),
            frame.arguments.getArgAsInt(0),
            frame.arguments.getArgAsWasmPtr(1),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun fdStatGet(
        memory: WasmMemory,
        fd: Int,
        dstAddr: WasmPtr,
    ): Int = handle.execute(memory.toHostMemory(), fd, dstAddr).code
}
