/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.fromRawDirFd
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.host.include.Fcntl.AT_REMOVEDIR
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public class SyscallUnlinkatFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_UNLINKAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirfd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        flags: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        val baseDirectory = BaseDirectory.fromRawDirFd(rawDirfd)
        return if (flags and AT_REMOVEDIR == AT_REMOVEDIR) {
            host.fileSystem.execute(
                operation = UnlinkDirectory,
                input = UnlinkDirectory(
                    path = path,
                    baseDirectory = baseDirectory,
                ),
            )
        } else {
            host.fileSystem.execute(
                operation = UnlinkFile,
                input = UnlinkFile(
                    path = path,
                    baseDirectory = baseDirectory,
                ),
            )
        }.negativeErrnoCode()
    }
}
