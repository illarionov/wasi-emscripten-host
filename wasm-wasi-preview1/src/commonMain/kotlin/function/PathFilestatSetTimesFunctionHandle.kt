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
import at.released.weh.wasi.preview1.type.Fstflags
import at.released.weh.wasi.preview1.type.FstflagsType
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasi.preview1.type.TimestampType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PathFilestatSetTimesFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_FILESTAT_SET_TIMES, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @LookupflagsType flags: Lookupflags,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
        @TimestampType atime: Timestamp,
        @TimestampType mtime: Timestamp,
        @FstflagsType fstflags: Fstflags,
    ): Errno {
        // TODO
        return Errno.NOTSUP
    }
}
