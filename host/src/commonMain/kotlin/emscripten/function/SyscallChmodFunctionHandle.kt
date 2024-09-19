/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.negativeErrnoCode
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString

public class SyscallChmodFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_CHMOD, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @FileMode mode: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)
        return host.fileSystem.execute(
            Chmod,
            Chmod(path, CurrentWorkingDirectory, mode),
        ).negativeErrnoCode()
    }
}
