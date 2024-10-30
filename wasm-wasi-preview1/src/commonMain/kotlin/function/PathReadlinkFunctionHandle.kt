/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.encodeToBuffer
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize

public class PathReadlinkFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_READLINK, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Byte::class) pathAddr: WasmPtr,
        pathSize: Int,
        @IntWasmPtr(Byte::class) bufAddr: WasmPtr,
        bufLen: Int,
        @IntWasmPtr(Int::class) sizeAddr: WasmPtr,
    ): Errno {
        val path = memory.readPathString(pathAddr, pathSize).getOrElse {
            return it
        }

        return host.fileSystem.execute(
            ReadLink,
            ReadLink(path, BaseDirectory.DirectoryFd(fd)),
        ).onRight { symlinkTarget ->
            val targetEncoded = symlinkTarget.encodeToBuffer()
            val size = targetEncoded.size.toInt().coerceAtMost(bufLen)
            memory.sinkWithMaxSize(bufAddr, size).use {
                it.write(targetEncoded, size.toLong())
            }
            memory.writeI32(sizeAddr, size)
        }.fold(
            ifLeft = FileSystemOperationError::wasiErrno,
            ifRight = { Errno.SUCCESS },
        )
    }
}
