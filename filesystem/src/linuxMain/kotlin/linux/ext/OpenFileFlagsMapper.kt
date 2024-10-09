/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.ext

import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ACCMODE
import at.released.weh.filesystem.op.opencreate.OpenFileFlags

private val openFileFlagsMaskToPosixMask = listOf(
    OpenFileFlag.O_CREAT to platform.posix.O_CREAT,
    OpenFileFlag.O_EXCL to platform.posix.O_EXCL,
    OpenFileFlag.O_NOCTTY to platform.posix.O_NOCTTY,
    OpenFileFlag.O_TRUNC to platform.posix.O_TRUNC,
    OpenFileFlag.O_APPEND to platform.posix.O_APPEND,
    OpenFileFlag.O_NONBLOCK to platform.posix.O_NONBLOCK,
    OpenFileFlag.O_NDELAY to platform.posix.O_NDELAY,
    OpenFileFlag.O_DSYNC to platform.posix.O_DSYNC,
    OpenFileFlag.O_ASYNC to platform.posix.O_ASYNC,
    OpenFileFlag.O_DIRECTORY to platform.posix.O_DIRECTORY,
    OpenFileFlag.O_NOFOLLOW to platform.posix.O_NOFOLLOW,
    OpenFileFlag.O_CLOEXEC to platform.posix.O_CLOEXEC,
    OpenFileFlag.O_SYNC to platform.posix.O_SYNC,
)

@Suppress("CyclomaticComplexMethod")
internal fun openFileFlagsToLinuxMask(
    @OpenFileFlags openFlags: Int,
): ULong {
    var mask = when (val mode = (openFlags and O_ACCMODE)) {
        OpenFileFlag.O_RDONLY -> platform.posix.O_RDONLY
        OpenFileFlag.O_WRONLY -> platform.posix.O_WRONLY
        OpenFileFlag.O_RDWR -> platform.posix.O_RDWR
        else -> error("Unknown mode $mode")
    }

    openFileFlagsMaskToPosixMask.forEach { (testMask, posixMask) ->
        if (openFlags and testMask == testMask) {
            mask = mask or posixMask
        }
    }
    // O_DIRECT, O_LARGEFILE, O_NOATIME: Not supported
    // O_PATH, O_TMPFILE: not supported, should be added?

    return mask.toULong()
}

@OpenFileFlags
internal fun linuxMaskToOpenFileFlags(
    linuxMask: Int,
): Int {
    var flags = when (val mode = (linuxMask and platform.posix.O_ACCMODE)) {
        platform.posix.O_RDONLY -> OpenFileFlag.O_RDONLY
        platform.posix.O_WRONLY -> OpenFileFlag.O_WRONLY
        platform.posix.O_RDWR -> OpenFileFlag.O_RDWR
        else -> error("Unknown mode $mode")
    }
    openFileFlagsMaskToPosixMask.forEach { (openFileFlagsMask, testFlag) ->
        if (linuxMask and testFlag == testFlag) {
            flags = flags or openFileFlagsMask
        }
    }
    return flags
}
