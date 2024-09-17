/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.fromRawDirFd

public class SyscallOpenatFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_OPENAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @OpenFileFlags rawFlags: Int,
        @FileMode rawMode: Int,
    ): Int {
        val fs = host.fileSystem
        val baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd)
        val path = memory.readNullTerminatedString(pathnamePtr)

        val fsOperation = Open(
            path = path,
            baseDirectory = baseDirectory,
            flags = rawFlags,
            mode = rawMode,
        )
        return fs.execute(Open, fsOperation)
            .fold(
                ifLeft = { error: OpenError -> -error.errno.code },
                ifRight = { it },
            )
    }
}
