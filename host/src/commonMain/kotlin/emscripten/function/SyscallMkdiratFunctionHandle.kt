/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.fromRawDirFd
import at.released.weh.host.ext.negativeErrnoCode

public class SyscallMkdiratFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_MKDIRAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @FileMode rawMode: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        return host.fileSystem.execute(
            operation = Mkdir,
            input = Mkdir(
                path = path,
                baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd),
                mode = rawMode,
            ),
        ).negativeErrnoCode()
    }
}
