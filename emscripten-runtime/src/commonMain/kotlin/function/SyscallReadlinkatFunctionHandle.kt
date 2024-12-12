/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import arrow.core.getOrElse
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_READLINKAT
import at.released.weh.emcripten.runtime.ext.fromRawDirFd
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileSystemErrno.Companion.wasiPreview1Code
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.readNullTerminatedString
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write

public class SyscallReadlinkatFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_READLINKAT, host) {
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
        val virtualPath = VirtualPath.create(path).getOrElse { _ -> return -Errno.INVAL.code }

        return host.fileSystem.execute(
            ReadLink,
            ReadLink(
                path = virtualPath,
                baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd),
            ),
        ).fold(
            ifLeft = { -it.errno.wasiPreview1Code },
        ) { linkPath: VirtualPath ->
            val linkpathBytes: ByteString = linkPath.utf8
            val len = linkpathBytes.size.toInt().coerceAtMost(bufSize)

            memory.sinkWithMaxSize(buf, len).buffered().use {
                it.write(linkpathBytes, 0, len)
            }
            len
        }
    }
}
