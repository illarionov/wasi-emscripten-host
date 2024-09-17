/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.wasi.function

import at.released.weh.bindings.graalvm240.ext.getArgAsInt
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.memory.GraalInputStreamWasiMemoryReader
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.memory.WasiMemoryReader
import at.released.weh.host.wasi.preview1.function.FdReadFdPreadFunctionHandle
import at.released.weh.host.wasi.preview1.type.Iovec
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal fun fdRead(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
): BaseWasmNode<FdReadFdPreadFunctionHandle> = FdRead(
    language,
    module,
    FdReadFdPreadFunctionHandle.fdRead(host),
)

internal fun fdPread(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
): BaseWasmNode<FdReadFdPreadFunctionHandle> = FdRead(
    language,
    module,
    FdReadFdPreadFunctionHandle.fdPread(host),
)

private class FdRead(
    language: WasmLanguage,
    module: WasmModule,
    handle: FdReadFdPreadFunctionHandle,
) : BaseWasmNode<FdReadFdPreadFunctionHandle>(language, module, handle) {
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
