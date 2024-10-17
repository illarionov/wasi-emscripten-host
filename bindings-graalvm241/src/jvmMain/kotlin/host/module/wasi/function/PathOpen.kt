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
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.function.PathOpenFunctionHandle
import at.released.weh.wasi.preview1.type.Fdflags
import at.released.weh.wasi.preview1.type.FdflagsType
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasi.preview1.type.Oflags
import at.released.weh.wasi.preview1.type.OflagsType
import at.released.weh.wasi.preview1.type.Rights
import at.released.weh.wasi.preview1.type.RightsType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory

internal class PathOpen(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
) : BaseWasiWasmNode<PathOpenFunctionHandle>(language, module, PathOpenFunctionHandle(host)) {
    @Suppress("MagicNumber")
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, wasmInstance: WasmInstance): Int {
        val args = frame.arguments
        return pathOpen(
            memory(frame),
            args.getArgAsInt(0),
            args.getArgAsInt(1),
            args.getArgAsWasmPtr(2),
            args.getArgAsInt(3),
            args.getArgAsInt(4).toShort(),
            args.getArgAsLong(5),
            args.getArgAsLong(6),
            args.getArgAsInt(7).toShort(),
            args.getArgAsWasmPtr(8),
        )
    }

    @TruffleBoundary
    @Suppress("MemberNameEqualsClassName")
    private fun pathOpen(
        memory: WasmMemory,
        @IntFileDescriptor fd: FileDescriptor,
        @LookupflagsType dirFlags: Lookupflags,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
        @OflagsType oflags: Oflags,
        @RightsType rights: Rights,
        @RightsType rightsInheriting: Rights,
        @FdflagsType fdflags: Fdflags,
        @IntWasmPtr(Int::class) expectedFdAddr: WasmPtr,
    ): Int = handle.execute(
        memory = memory.toHostMemory(),
        fd = fd,
        dirFlags = dirFlags,
        path = path,
        pathSize = pathSize,
        oflags = oflags,
        rights = rights,
        rightsInheriting = rightsInheriting,
        fdflags = fdflags,
        expectedFdAddr = expectedFdAddr,
    ).code
}
