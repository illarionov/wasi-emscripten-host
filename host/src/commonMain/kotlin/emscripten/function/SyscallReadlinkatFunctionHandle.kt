/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.ext.fromRawDirFd
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.readNullTerminatedString
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.Buffer
import kotlinx.io.writeString

public class SyscallReadlinkatFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.SYSCALL_READLINKAT, host) {
    public fun execute(
        memory: Memory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        @IntWasmPtr(Byte::class) buf: WasmPtr,
        bufSize: Int,
    ): Int {
        val path = memory.readNullTerminatedString(pathnamePtr)

        if (bufSize < 0) {
            return -Errno.INVAL.code
        }

        return host.fileSystem.execute(
            ReadLink,
            ReadLink(
                path = path,
                baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd),
            ),
        ).fold(
            ifLeft = { -it.errno.code },
        ) { linkPath: String ->
            val linkpathBuffer = Buffer().also { it.writeString(linkPath.toString()) }
            val len = linkpathBuffer.size.toInt().coerceAtMost(bufSize)

            memory.sinkWithMaxSize(buf, len).use {
                it.write(linkpathBuffer, len.toLong())
            }
            len
        }
    }
}
