/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.ext

import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType

private val fsFdFlagsMaskToPosixMask = listOf(
    FdFlag.FD_APPEND to platform.posix.O_APPEND,
    FdFlag.FD_NONBLOCK to platform.posix.O_NONBLOCK,
    FdFlag.FD_DSYNC to platform.posix.O_DSYNC,
    FdFlag.FD_SYNC to platform.posix.O_SYNC,
    FdFlag.FD_RSYNC to platform.posix.O_RSYNC,
)

@Suppress("CyclomaticComplexMethod")
internal fun fdFdFlagsToLinuxMask(
    @FdflagsType openFlags: Fdflags,
): ULong {
    var mask = 0
    fsFdFlagsMaskToPosixMask.forEach { (testMask, posixMask) ->
        if (openFlags and testMask == testMask) {
            mask = mask or posixMask
        }
    }
    return mask.toULong()
}

@FdflagsType
internal fun linuxMaskToFsFdFlags(
    linuxMask: Int,
): Int {
    var flags: Fdflags = 0
    fsFdFlagsMaskToPosixMask.forEach { (openFileFlagsMask, testFlag) ->
        if (linuxMask and testFlag == testFlag) {
            flags = flags or openFileFlagsMask
        }
    }
    return flags
}
