/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.wasi.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsLong
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.wasi.BaseWasiWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdSeekFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class FdSeek(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasiWasmNode<FdSeekFunctionHandle>(language, module, FdSeekFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        val args = frame.arguments
        return fdSeek(
            memory(frame),
            args.getArgAsInt(0),
            args.getArgAsLong(1),
            args.getArgAsInt(2).toByte(),
            args.getArgAsWasmPtr(3),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun fdSeek(
        memory: WasmMemory,
        fd: Int,
        offset: Long,
        whenceByte: Byte,
        @IntWasmPtr(Long::class) pNewOffset: WasmPtr,
    ): Int = handle.execute(memory.toHostMemory(), fd, offset, whenceByte, pNewOffset).code
}
