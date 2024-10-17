/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasi.preview1.type.Riflags
import at.released.weh.wasi.preview1.type.RiflagsType
import at.released.weh.wasi.preview1.type.Roflags
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class SockRecvFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.SOCK_RECV, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Iovec::class) riData: WasmPtr,
        riDataSize: Int,
        @RiflagsType riFlags: Riflags,
        @IntWasmPtr(Size::class) expectedSizeAddr: WasmPtr,
        @IntWasmPtr(Roflags::class) expectedRoflagsAddr: WasmPtr,
    ): Errno {
        // TODO
        return Errno.NOTSUP
    }
}
