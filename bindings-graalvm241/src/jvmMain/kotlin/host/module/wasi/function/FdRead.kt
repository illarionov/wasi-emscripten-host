/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.wasi.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.memory.GraalInputStreamWasiMemoryReader
import at.released.weh.bindings.graalvm241.host.module.wasi.BaseWasiWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdReadFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class FdRead(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasiWasmNode<FdReadFunctionHandle>(language, module, FdReadFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        val args = frame.arguments
        return fdRead(
            memory(frame),
            args.getArgAsInt(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsInt(2),
            args.getArgAsWasmPtr(3),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun fdRead(
        memory: WasmMemory,
        fd: Int,
        @IntWasmPtr(Iovec::class) pIov: WasmPtr,
        iovCnt: Int,
        @IntWasmPtr(Int::class) pNum: WasmPtr,
    ): Int {
        val hostMemory = memory.toHostMemory()
        val wasiMemoryReader: WasiMemoryReader = GraalInputStreamWasiMemoryReader(hostMemory, handle.host.fileSystem)
        return handle.execute(hostMemory, wasiMemoryReader, fd, pIov, iovCnt, pNum).code
    }
}
