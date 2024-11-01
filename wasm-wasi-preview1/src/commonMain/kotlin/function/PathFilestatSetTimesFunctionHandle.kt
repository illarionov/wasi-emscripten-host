/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.identity
import arrow.core.raise.either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.getRequestedAtimeMtime
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.Fstflags
import at.released.weh.wasi.preview1.type.FstflagsType
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsFlag.SYMLINK_FOLLOW
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasi.preview1.type.TimestampType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory

public class PathFilestatSetTimesFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.PATH_FILESTAT_SET_TIMES, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @LookupflagsType flags: Lookupflags,
        @IntWasmPtr(Byte::class) pathAddr: WasmPtr,
        pathSize: Int,
        @TimestampType atime: Timestamp,
        @TimestampType mtime: Timestamp,
        @FstflagsType fstflags: Fstflags,
    ): Errno = either {
        val path = memory.readPathString(pathAddr, pathSize).bind()
        val reqTimestamps = getRequestedAtimeMtime(host.clock, atime, mtime, fstflags).bind()
        host.fileSystem.execute(
            SetTimestamp,
            SetTimestamp(
                path = path,
                baseDirectory = BaseDirectory.DirectoryFd(fd),
                atimeNanoseconds = reqTimestamps.atimeNanoseconds,
                mtimeNanoseconds = reqTimestamps.mtimeNanoseconds,
                followSymlinks = flags and SYMLINK_FOLLOW == SYMLINK_FOLLOW,
            ),
        ).fold(
            ifLeft = FileSystemOperationError::wasiErrno,
            ifRight = { SUCCESS },
        )
    }.fold(::identity, ::identity)
}
