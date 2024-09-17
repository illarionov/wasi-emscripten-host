/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode

public class SyscallRmdirFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_RMDIR, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        return host.fileSystem.execute(
            operation = UnlinkDirectory,
            input = UnlinkDirectory(
                path = path,
                baseDirectory = CurrentWorkingDirectory,
            ),
        ).negativeErrnoCode()
    }
}
