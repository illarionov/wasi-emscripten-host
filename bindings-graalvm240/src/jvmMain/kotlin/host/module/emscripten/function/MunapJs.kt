/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.emscripten.function

import at.released.weh.bindings.graalvm240.ext.getArgAsInt
import at.released.weh.bindings.graalvm240.ext.getArgAsLong
import at.released.weh.bindings.graalvm240.ext.getArgAsUint
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.emscripten.function.MunapJsFunctionHandle
import at.released.weh.host.include.sys.SysMmanMapFlags
import at.released.weh.host.include.sys.SysMmanProt
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
            args.getArgAsUint(2),
            args.getArgAsUint(3),
            args.getArgAsInt(4),
            args.getArgAsLong(5).toULong(),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun munmapJs(
        addr: WasmPtr<Byte>,
        len: Int,
        prot: UInt,
        flags: UInt,
        fd: Int,
        offset: ULong,
    ): Int = handle.execute(addr, len, SysMmanProt(prot), SysMmanMapFlags(flags), Fd(fd), offset)
}