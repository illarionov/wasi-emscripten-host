/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.wasi.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.memory.GraalOutputStreamWasiMemoryWriter
import at.released.weh.bindings.graalvm241.host.module.wasi.BaseWasiWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.FdWriteFdPWriteFunctionHandle
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasi.preview1.type.CioVec
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal fun fdWrite(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
): BaseWasiWasmNode<FdWriteFdPWriteFunctionHandle> =
    FdWrite(language, module, FdWriteFdPWriteFunctionHandle.fdWrite(host))

internal fun fdPwrite(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
): BaseWasiWasmNode<FdWriteFdPWriteFunctionHandle> =
    FdWrite(language, module, FdWriteFdPWriteFunctionHandle.fdPwrite(host))

private class FdWrite(
    language: WasmLanguage,
    module: WasmModule,
    handle: FdWriteFdPWriteFunctionHandle,
) : BaseWasiWasmNode<FdWriteFdPWriteFunctionHandle>(language, module, handle) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        val args = frame.arguments
        return fdWrite(
            memory(frame),
            args.getArgAsInt(0),
            args.getArgAsWasmPtr(1),
            args.getArgAsInt(2),
            args.getArgAsWasmPtr(3),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun fdWrite(
        memory: WasmMemory,
        fd: Int,
        @IntWasmPtr(CioVec::class) pCiov: WasmPtr,
        cIovCnt: Int,
        @IntWasmPtr(Int::class) pNum: WasmPtr,
    ): Int {
        val hostMemory = memory.toHostMemory()
        val wasiMemoryWriter: WasiMemoryWriter = GraalOutputStreamWasiMemoryWriter(
            hostMemory,
            handle.host.fileSystem,
            handle.host.rootLogger,
        )
        return handle.execute(hostMemory, wasiMemoryWriter, fd, pCiov, cIovCnt, pNum).code
    }
}
