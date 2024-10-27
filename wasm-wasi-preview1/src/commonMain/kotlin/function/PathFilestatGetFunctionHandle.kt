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
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.timeNanos
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.PATH_FILESTAT_GET
import at.released.weh.wasi.preview1.ext.FILESTAT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.packTo
import at.released.weh.wasi.preview1.ext.readPathString
import at.released.weh.wasi.preview1.ext.toWasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.Filestat
import at.released.weh.wasi.preview1.type.Filetype
import at.released.weh.wasi.preview1.type.Lookupflags
import at.released.weh.wasi.preview1.type.LookupflagsType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class PathFilestatGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(PATH_FILESTAT_GET, host) {
    @Suppress("UNUSED_PARAMETER")
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @LookupflagsType flags: Lookupflags,
        @IntWasmPtr(Byte::class) path: WasmPtr,
        pathSize: Int,
        @IntWasmPtr(Filestat::class) filestatAddr: WasmPtr,
    ): Errno {
        val pathString = memory.readPathString(path, pathSize).getOrElse {
            return it
        }

        return host.fileSystem.execute(Stat, Stat(pathString, DirectoryFd(fd)))
            .onRight { stat ->
                memory.sinkWithMaxSize(filestatAddr, FILESTAT_PACKED_SIZE).buffered().use {
                    stat.toFilestat().packTo(it)
                }
            }
            .fold(
                ifLeft = { it.errno.toWasiErrno() },
                ifRight = { SUCCESS },
            )
    }

    private companion object {
        fun StructStat.toFilestat(): Filestat = Filestat(
            dev = this.deviceId,
            ino = this.inode,
            filetype = checkNotNull(Filetype.fromCode(this.type.id)) {
                "Unexpected type ${this.type.id}"
            },
            nlink = this.links,
            size = this.size,
            atim = this.accessTime.timeNanos,
            mtim = this.modificationTime.timeNanos,
            ctim = this.changeStatusTime.timeNanos,
        )
    }
}
