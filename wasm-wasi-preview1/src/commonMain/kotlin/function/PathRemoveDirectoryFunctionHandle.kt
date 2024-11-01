/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PathRemoveDirectoryFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_REMOVE_DIRECTORY, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
    ): Errno {
        val pathString = memory.readPathString(path, pathSize).getOrElse {
            return it
        }
        return host.fileSystem.execute(
            UnlinkDirectory,
            UnlinkDirectory(pathString, DirectoryFd(fd)),
        ).foldToErrno()
    }
}
