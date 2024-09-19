/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.emscripten.function.SyscallUnlinkatFunctionHandle
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class SyscallUnlinkat(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<SyscallUnlinkatFunctionHandle>(language, module, SyscallUnlinkatFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        val args = frame.arguments
        return syscallUnlinkat(
            memory(frame),
            args.getArgAsInt(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsInt(2),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun syscallUnlinkat(
        memory: WasmMemory,
        rawDirfd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        flags: Int,
    ): Int = handle.execute(memory.toHostMemory(), rawDirfd, pathnamePtr, flags)
}
