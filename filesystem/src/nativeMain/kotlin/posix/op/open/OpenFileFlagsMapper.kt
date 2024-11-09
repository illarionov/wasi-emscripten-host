/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.open

import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ACCMODE
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType

internal expect val openFileFlagsMaskToPosixMask: List<Pair<Int, Int>>

@Suppress("CyclomaticComplexMethod")
internal fun openFileFlagsToPosixMask(
    @OpenFileFlagsType openFlags: Int,
): ULong {
    val openMode = when (val mode = (openFlags and O_ACCMODE)) {
        OpenFileFlag.O_RDONLY -> platform.posix.O_RDONLY
        OpenFileFlag.O_WRONLY -> platform.posix.O_WRONLY
        OpenFileFlag.O_RDWR -> platform.posix.O_RDWR
        else -> error("Unknown mode $mode")
    }

    // O_DIRECT, O_LARGEFILE, O_NOATIME: Not supported
    // O_PATH, O_TMPFILE: not supported, should be added?
    return openFileFlagsMaskToPosixMask.fold(openMode) { mask, (testMask, posixMask) ->
        if (openFlags and testMask == testMask) {
            if (posixMask != 0) {
                mask or posixMask
            } else {
                error("Flag $testMask of open mode is not supported")
            }
        } else {
            mask
        }
    }.toULong()
}

@OpenFileFlagsType
internal fun posixMaskToOpenFileFlags(
    posixMask: Int,
): Int {
    val openMode = when (val mode = (posixMask and platform.posix.O_ACCMODE)) {
        platform.posix.O_RDONLY -> OpenFileFlag.O_RDONLY
        platform.posix.O_WRONLY -> OpenFileFlag.O_WRONLY
        platform.posix.O_RDWR -> OpenFileFlag.O_RDWR
        else -> error("Unknown mode $mode")
    }
    return openFileFlagsMaskToPosixMask.fold(openMode) { mask, (openFileFlagsMask, testFlag) ->
        if (testFlag != 0 && posixMask and testFlag == testFlag) {
            mask or openFileFlagsMask
        } else {
            mask
        }
    }
}
