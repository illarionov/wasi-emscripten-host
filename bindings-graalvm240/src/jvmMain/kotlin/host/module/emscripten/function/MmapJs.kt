/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.emscripten.function

import at.released.weh.bindings.graalvm240.ext.getArgAsInt
import at.released.weh.bindings.graalvm240.ext.getArgAsLong
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.emscripten.function.MmapJsFunctionHandle
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class MmapJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<MmapJsFunctionHandle>(language, module, MmapJsFunctionHandle(host)) {
    @Suppress("MagicNumber")
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Int {
        val args = frame.arguments
        return mmapJs(
            args.getArgAsInt(0),
            args.getArgAsInt(1),
            args.getArgAsInt(2),
            args.getArgAsInt(3),
            args.getArgAsLong(4),
            args.getArgAsWasmPtr(5),
            args.getArgAsWasmPtr(6),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun mmapJs(
        len: Int,
        prot: Int,
        flags: Int,
        fd: Int,
        offset: Long,
        @IntWasmPtr(Int::class) pAllocated: WasmPtr,
        @IntWasmPtr(WasmPtr::class) pAddr: WasmPtr,
    ): Int = handle.execute(len, prot, flags, fd, offset, pAllocated, pAddr)
}
