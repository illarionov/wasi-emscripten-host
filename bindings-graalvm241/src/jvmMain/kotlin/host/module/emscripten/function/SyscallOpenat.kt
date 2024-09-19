/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.host.module.emscripten.function

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm241.host.module.emscripten.BaseEmscriptenWasmNode
import at.released.weh.emcripten.runtime.function.SyscallOpenatFunctionHandle
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmArguments
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class SyscallOpenat(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseEmscriptenWasmNode<SyscallOpenatFunctionHandle>(language, module, SyscallOpenatFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        val args = frame.arguments
        val memory = memory(frame)
        val mode = if (WasmArguments.getArgumentCount(args) >= 4) {
            memory.load_i32(this, args.getArgAsInt(3).toLong())
        } else {
            0
        }

        val fdOrErrno = openAt(
            memory,
            rawDirFd = args.getArgAsInt(0),
            pathnamePtr = args.getArgAsWasmPtr(1),
            flags = args.getArgAsInt(2),
            rawMode = mode,
        )
        return fdOrErrno
    }

    @TruffleBoundary
    private fun openAt(
        memory: WasmMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @OpenFileFlags flags: Int,
        @FileMode rawMode: Int,
    ): Int = handle.execute(memory.toHostMemory(), rawDirFd, pathnamePtr, flags, rawMode)
}
