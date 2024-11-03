/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Rights
import at.released.weh.wasi.preview1.type.RightsType
import at.released.weh.filesystem.fdrights.FdRightsFlag as FsRightsFlag
import at.released.weh.filesystem.fdrights.FdRightsType as FsRightsType
import at.released.weh.wasi.preview1.type.RightsFlag as WasiRightsFlag

internal object WasiRightsMapper {
    private val wasiRightsToFsRights = listOf(
        WasiRightsFlag.FD_DATASYNC to FsRightsFlag.FD_DATASYNC,
        WasiRightsFlag.FD_READ to FsRightsFlag.FD_READ,
        WasiRightsFlag.FD_SEEK to FsRightsFlag.FD_SEEK,
        WasiRightsFlag.FD_FDSTAT_SET_FLAGS to FsRightsFlag.FD_FDSTAT_SET_FLAGS,
        WasiRightsFlag.FD_SYNC to FsRightsFlag.FD_SYNC,
        WasiRightsFlag.FD_TELL to FsRightsFlag.FD_TELL,
        WasiRightsFlag.FD_WRITE to FsRightsFlag.FD_WRITE,
        WasiRightsFlag.FD_ADVISE to FsRightsFlag.FD_ADVISE,
        WasiRightsFlag.FD_ALLOCATE to FsRightsFlag.FD_ALLOCATE,
        WasiRightsFlag.PATH_CREATE_DIRECTORY to FsRightsFlag.PATH_CREATE_DIRECTORY,
        WasiRightsFlag.PATH_CREATE_FILE to FsRightsFlag.PATH_CREATE_FILE,
        WasiRightsFlag.PATH_LINK_SOURCE to FsRightsFlag.PATH_LINK_SOURCE,
        WasiRightsFlag.PATH_LINK_TARGET to FsRightsFlag.PATH_LINK_TARGET,
        WasiRightsFlag.PATH_OPEN to FsRightsFlag.PATH_OPEN,
        WasiRightsFlag.FD_READDIR to FsRightsFlag.FD_READDIR,
        WasiRightsFlag.PATH_READLINK to FsRightsFlag.PATH_READLINK,
        WasiRightsFlag.PATH_RENAME_SOURCE to FsRightsFlag.PATH_RENAME_SOURCE,
        WasiRightsFlag.PATH_RENAME_TARGET to FsRightsFlag.PATH_RENAME_TARGET,
        WasiRightsFlag.PATH_FILESTAT_GET to FsRightsFlag.PATH_FILESTAT_GET,
        WasiRightsFlag.PATH_FILESTAT_SET_SIZE to FsRightsFlag.PATH_FILESTAT_SET_SIZE,
        WasiRightsFlag.PATH_FILESTAT_SET_TIMES to FsRightsFlag.PATH_FILESTAT_SET_TIMES,
        WasiRightsFlag.FD_FILESTAT_GET to FsRightsFlag.FD_FILESTAT_GET,
        WasiRightsFlag.FD_FILESTAT_SET_SIZE to FsRightsFlag.FD_FILESTAT_SET_SIZE,
        WasiRightsFlag.FD_FILESTAT_SET_TIMES to FsRightsFlag.FD_FILESTAT_SET_TIMES,
        WasiRightsFlag.PATH_SYMLINK to FsRightsFlag.PATH_SYMLINK,
        WasiRightsFlag.PATH_REMOVE_DIRECTORY to FsRightsFlag.PATH_REMOVE_DIRECTORY,
        WasiRightsFlag.PATH_UNLINK_FILE to FsRightsFlag.PATH_UNLINK_FILE,
        WasiRightsFlag.POLL_FD_READWRITE to FsRightsFlag.POLL_FD_READWRITE,
        WasiRightsFlag.SOCK_SHUTDOWN to FsRightsFlag.SOCK_SHUTDOWN,
        WasiRightsFlag.SOCK_ACCEPT to FsRightsFlag.SOCK_ACCEPT,
    )

    @FsRightsType
    fun getFsRights(
        @RightsType wasiRights: Rights,
    ): Long = wasiRightsToFsRights.fold(0L) { mask, (wasiMask, fsMask) ->
        if (wasiRights and wasiMask == wasiMask) {
            mask or fsMask
        } else {
            mask
        }
    }
}
