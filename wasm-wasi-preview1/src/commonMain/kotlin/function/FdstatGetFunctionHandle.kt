/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.model.Filetype.BLOCK_DEVICE
import at.released.weh.filesystem.model.Filetype.CHARACTER_DEVICE
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.model.Filetype.SOCKET_DGRAM
import at.released.weh.filesystem.model.Filetype.SOCKET_STREAM
import at.released.weh.filesystem.model.Filetype.SYMBOLIC_LINK
import at.released.weh.filesystem.model.Filetype.UNKNOWN
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.fdattributes.FdRights
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag
import at.released.weh.filesystem.op.fdattributes.FdRightsType
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_FDSTAT_GET
import at.released.weh.wasi.preview1.ext.FDSTAT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.packTo
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.Fdflags
import at.released.weh.wasi.preview1.type.FdflagsFlag
import at.released.weh.wasi.preview1.type.Fdstat
import at.released.weh.wasi.preview1.type.Rights
import at.released.weh.wasi.preview1.type.RightsFlag
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered
import kotlin.experimental.or
import at.released.weh.wasi.preview1.type.Filetype as WasiFiletype

public class FdstatGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(FD_FDSTAT_GET, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Fdstat::class) dstAddr: WasmPtr,
    ): Errno {
        return host.fileSystem.execute(FdAttributes, FdAttributes(fd))
            .onRight { prestatResult: FdAttributesResult ->
                memory.sinkWithMaxSize(dstAddr, FDSTAT_PACKED_SIZE).buffered().use {
                    prestatResult.toFdStat().packTo(it)
                }
            }.fold(
                ifLeft = FdAttributesError::wasiErrno,
                ifRight = { _ -> SUCCESS },
            )
    }

    private companion object {
        private val openFileFlagsToFdFlagsMap = listOf(
            OpenFileFlag.O_APPEND to FdflagsFlag.APPEND,
            OpenFileFlag.O_DSYNC to FdflagsFlag.DSYNC,
            OpenFileFlag.O_NONBLOCK to FdflagsFlag.NONBLOCK,
            OpenFileFlag.O_SYNC to FdflagsFlag.SYNC,
        )
        private val filesystemToWasiRights = listOf(
            FdRightsFlag.FD_DATASYNC to RightsFlag.FD_DATASYNC,
            FdRightsFlag.FD_READ to RightsFlag.FD_READ,
            FdRightsFlag.FD_SEEK to RightsFlag.FD_SEEK,
            FdRightsFlag.FD_FDSTAT_SET_FLAGS to RightsFlag.FD_FDSTAT_SET_FLAGS,
            FdRightsFlag.FD_SYNC to RightsFlag.FD_SYNC,
            FdRightsFlag.FD_TELL to RightsFlag.FD_TELL,
            FdRightsFlag.FD_WRITE to RightsFlag.FD_WRITE,
            FdRightsFlag.FD_ADVISE to RightsFlag.FD_ADVISE,
            FdRightsFlag.FD_ALLOCATE to RightsFlag.FD_ALLOCATE,
            FdRightsFlag.PATH_CREATE_DIRECTORY to RightsFlag.PATH_CREATE_DIRECTORY,
            FdRightsFlag.PATH_CREATE_FILE to RightsFlag.PATH_CREATE_FILE,
            FdRightsFlag.PATH_LINK_SOURCE to RightsFlag.PATH_LINK_SOURCE,
            FdRightsFlag.PATH_LINK_TARGET to RightsFlag.PATH_LINK_TARGET,
            FdRightsFlag.PATH_OPEN to RightsFlag.PATH_OPEN,
            FdRightsFlag.FD_READDIR to RightsFlag.FD_READDIR,
            FdRightsFlag.PATH_READLINK to RightsFlag.PATH_READLINK,
            FdRightsFlag.PATH_RENAME_SOURCE to RightsFlag.PATH_RENAME_SOURCE,
            FdRightsFlag.PATH_RENAME_TARGET to RightsFlag.PATH_RENAME_TARGET,
            FdRightsFlag.PATH_FILESTAT_GET to RightsFlag.PATH_FILESTAT_GET,
            FdRightsFlag.PATH_FILESTAT_SET_SIZE to RightsFlag.PATH_FILESTAT_SET_SIZE,
            FdRightsFlag.PATH_FILESTAT_SET_TIMES to RightsFlag.PATH_FILESTAT_SET_TIMES,
            FdRightsFlag.FD_FILESTAT_GET to RightsFlag.FD_FILESTAT_GET,
            FdRightsFlag.FD_FILESTAT_SET_SIZE to RightsFlag.FD_FILESTAT_SET_SIZE,
            FdRightsFlag.FD_FILESTAT_SET_TIMES to RightsFlag.FD_FILESTAT_SET_TIMES,
            FdRightsFlag.PATH_SYMLINK to RightsFlag.PATH_SYMLINK,
            FdRightsFlag.PATH_REMOVE_DIRECTORY to RightsFlag.PATH_REMOVE_DIRECTORY,
            FdRightsFlag.PATH_UNLINK_FILE to RightsFlag.PATH_UNLINK_FILE,
            FdRightsFlag.POLL_FD_READWRITE to RightsFlag.POLL_FD_READWRITE,
            FdRightsFlag.SOCK_SHUTDOWN to RightsFlag.SOCK_SHUTDOWN,
            FdRightsFlag.SOCK_ACCEPT to RightsFlag.SOCK_ACCEPT,
        )

        private fun FdAttributesResult.toFdStat(): Fdstat = Fdstat(
            fsFiletype = this.type.toWasiType(),
            fsFlags = this.flags.toWasiOpenFlags(),
            fsRightsBase = this.rights.toWasiRights(),
            fsRightsInheriting = this.inheritingRights.toWasiRights(),
        )

        private fun Filetype.toWasiType(): WasiFiletype = when (this) {
            UNKNOWN -> WasiFiletype.UNKNOWN
            BLOCK_DEVICE -> WasiFiletype.BLOCK_DEVICE
            CHARACTER_DEVICE -> WasiFiletype.CHARACTER_DEVICE
            DIRECTORY -> WasiFiletype.DIRECTORY
            REGULAR_FILE -> WasiFiletype.REGULAR_FILE
            SOCKET_DGRAM -> WasiFiletype.SOCKET_DGRAM
            SOCKET_STREAM -> WasiFiletype.SOCKET_STREAM
            SYMBOLIC_LINK -> WasiFiletype.SYMBOLIC_LINK
        }

        @OpenFileFlags
        private fun Int.toWasiOpenFlags(): Fdflags {
            var mask: Fdflags = 0
            openFileFlagsToFdFlagsMap.forEach { (openFileFlag, fdFlag) ->
                if (this and openFileFlag == openFileFlag) {
                    mask = mask or fdFlag
                }
            }
            return mask
        }

        @FdRightsType
        private fun FdRights.toWasiRights(): Rights {
            var mask: Rights = 0
            filesystemToWasiRights.forEach { (fsFlag, wasiFlag) ->
                if (this and fsFlag == fsFlag) {
                    mask = mask or wasiFlag
                }
            }
            return mask
        }
    }
}
