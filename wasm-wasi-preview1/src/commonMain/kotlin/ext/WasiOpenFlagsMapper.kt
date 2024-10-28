/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.wasi.preview1.type.OflagsFlag
import at.released.weh.wasi.preview1.type.OflagsType
import at.released.weh.wasi.preview1.type.RightsType
import kotlin.experimental.and
import at.released.weh.filesystem.op.opencreate.OpenFileFlag as FsOpenFileFlag
import at.released.weh.wasi.preview1.type.Oflags as WasiOflags
import at.released.weh.wasi.preview1.type.Rights as WasiRights
import at.released.weh.wasi.preview1.type.RightsFlag as WasiRightsFlag

internal object WasiOpenFlagsMapper {
    private val wasiOflagsToFsOpenFlags = listOf(
        OflagsFlag.CREAT to FsOpenFileFlag.O_CREAT,
        OflagsFlag.DIRECTORY to FsOpenFileFlag.O_DIRECTORY,
        OflagsFlag.EXCL to FsOpenFileFlag.O_EXCL,
        OflagsFlag.TRUNC to FsOpenFileFlag.O_TRUNC,
    )

    @OpenFileFlagsType
    fun getFsOpenFlags(
        @OflagsType wasiOpenFlags: WasiOflags,
        @RightsType wasiFdRights: WasiRights,
    ): Int {
        val wasiFlags = getFsOpenFlagsBase(wasiOpenFlags)
        val isDirectory = (wasiFlags and FsOpenFileFlag.O_DIRECTORY == FsOpenFileFlag.O_DIRECTORY)

        val openMode = if (!isDirectory) {
            when (val fdRightMode = (wasiFdRights and (WasiRightsFlag.FD_WRITE or WasiRightsFlag.FD_READ))) {
                0L -> 0
                WasiRightsFlag.FD_WRITE or WasiRightsFlag.FD_READ -> FsOpenFileFlag.O_RDWR
                WasiRightsFlag.FD_WRITE -> FsOpenFileFlag.O_WRONLY
                WasiRightsFlag.FD_READ -> FsOpenFileFlag.O_RDONLY
                else -> error("Unexpected mode $fdRightMode")
            }
        } else {
            0
        }

        return wasiFlags or openMode
    }

    @OpenFileFlagsType
    private fun getFsOpenFlagsBase(
        @OflagsType wasiOpenFlags: WasiOflags,
    ): Int = wasiOflagsToFsOpenFlags.fold(0) { mask, (oflagMask, fsOpenFileFlagMask) ->
        if (wasiOpenFlags and oflagMask == oflagMask) {
            mask or fsOpenFileFlagMask
        } else {
            mask
        }
    }
}
