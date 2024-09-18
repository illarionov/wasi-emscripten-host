/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsLong
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.MunapJsFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal class MunapJs(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasmNode<MunapJsFunctionHandle>(language, module, MunapJsFunctionHandle(host)) {
    @Suppress("MagicNumber")
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Int {
        val args = frame.arguments
        return munmapJs(
            args.getArgAsWasmPtr(0),
            args.getArgAsInt(1),
            args.getArgAsInt(2),
            args.getArgAsInt(3),
            args.getArgAsInt(4),
            args.getArgAsLong(5),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun munmapJs(
        @IntWasmPtr(Byte::class) addr: WasmPtr,
        len: Int,
        prot: Int,
        flags: Int,
        fd: Int,
        offset: Long,
    ): Int = handle.execute(addr, len, prot, flags, fd, offset)
}
