/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import arrow.core.getOrElse
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_UNLINKAT
import at.released.weh.emcripten.runtime.ext.fromRawDirFd
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.emcripten.runtime.include.Fcntl.AT_REMOVEDIR
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString

public class SyscallUnlinkatFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_UNLINKAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirfd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        flags: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        val virtualPath = VirtualPath.create(path).getOrElse { _ -> return -Errno.INVAL.code }

        val baseDirectory = BaseDirectory.fromRawDirFd(rawDirfd)
        return if (flags and AT_REMOVEDIR == AT_REMOVEDIR) {
            host.fileSystem.execute(
                operation = UnlinkDirectory,
                input = UnlinkDirectory(
                    path = virtualPath,
                    baseDirectory = baseDirectory,
                ),
            )
        } else {
            host.fileSystem.execute(
                operation = UnlinkFile,
                input = UnlinkFile(
                    path = virtualPath,
                    baseDirectory = baseDirectory,
                ),
            )
        }.negativeErrnoCode()
    }
}
