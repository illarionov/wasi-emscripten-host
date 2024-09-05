/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module.emscripten.function

import at.released.weh.bindings.graalvm240.ext.getArgAsInt
import at.released.weh.bindings.graalvm240.ext.getArgAsUint
import at.released.weh.bindings.graalvm240.ext.getArgAsWasmPtr
import at.released.weh.bindings.graalvm240.host.module.BaseWasmNode
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.emscripten.function.SyscallOpenatFunctionHandle
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
) : BaseWasmNode<SyscallOpenatFunctionHandle>(language, module, SyscallOpenatFunctionHandle(host)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance): Any {
        val args = frame.arguments
        val memory = memory(frame)
        val mode = if (WasmArguments.getArgumentCount(args) >= 4) {
            memory.load_i32(this, args.getArgAsInt(3).toLong()).toUInt()
        } else {
            0U
        }

        val fdOrErrno = openAt(
            memory,
            rawDirFd = args.getArgAsInt(0),
            pathnamePtr = args.getArgAsWasmPtr(1),
            flags = args.getArgAsUint(2),
            rawMode = mode,
        )
        return fdOrErrno
    }

    @TruffleBoundary
    private fun openAt(
        memory: WasmMemory,
        rawDirFd: Int,
        pathnamePtr: WasmPtr<Byte>,
        flags: UInt,
        rawMode: UInt,
    ): Int = handle.execute(memory.toHostMemory(), rawDirFd, pathnamePtr, flags, rawMode)
}
