/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import arrow.core.raise.either
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PathRenameFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_RENAME, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
        @IntFileDescriptor newFd: FileDescriptor,
        @IntWasmPtr(Byte::class) newPath: WasmPtr,
        newPathSize: Int,
    ): Errno = either {
        val oldPathString = memory.readPathString(path, pathSize).bind()
        val newPathString = memory.readPathString(newPath, newPathSize).bind()
        host.fileSystem.execute(
            Rename,
            Rename(
                BaseDirectory.DirectoryFd(fd),
                oldPathString,
                BaseDirectory.DirectoryFd(newFd),
                newPathString,
            ),
        ).foldToErrno()
    }.getOrElse { it }
}
