/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import arrow.core.getOrElse
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.SYSCALL_OPENAT
import at.released.weh.emcripten.runtime.ext.EmscriptenFdFlagsMapper
import at.released.weh.emcripten.runtime.ext.EmscriptenOpenFileFlagsMapper
import at.released.weh.emcripten.runtime.ext.fromRawDirFd
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.FileSystemErrno.Companion.wasiPreview1Code
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString

public class SyscallOpenatFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(SYSCALL_OPENAT, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        rawDirFd: Int,
        @IntWasmPtr(Byte::class) pathnamePtr: WasmPtr,
        rawFlags: Int,
        @FileMode rawMode: Int,
    ): Int {
        val fs = host.fileSystem
        val baseDirectory = BaseDirectory.fromRawDirFd(rawDirFd)
        val path = memory.readNullTerminatedString(pathnamePtr)

        val virtualPath = VirtualPath.create(path).getOrElse { _ -> return -Errno.INVAL.code }

        val fsOperation = Open(
            path = virtualPath,
            baseDirectory = baseDirectory,
            openFlags = EmscriptenOpenFileFlagsMapper.getOpenFlags(rawFlags),
            fdFlags = EmscriptenFdFlagsMapper.getFdFlags(rawFlags),
            mode = rawMode,
        )
        return fs.execute(Open, fsOperation)
            .fold(
                ifLeft = { error: OpenError -> -error.errno.wasiPreview1Code },
                ifRight = { it },
            )
    }
}
