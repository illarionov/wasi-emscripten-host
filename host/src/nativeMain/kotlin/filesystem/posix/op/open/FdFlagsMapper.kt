/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.open

import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType

internal expect val fsFdFlagsMaskToPosixMask: List<Pair<Int, Int>>

@Suppress("CyclomaticComplexMethod")
internal fun fdFdFlagsToPosixMask(
    @FdflagsType openFlags: Fdflags,
): ULong {
    var mask = 0
    fsFdFlagsMaskToPosixMask.forEach { (testMask, posixMask) ->
        if (openFlags and testMask == testMask) {
            if (posixMask != 0) {
                mask = mask or posixMask
            } else {
                error("Flag $testMask of fdflags is not supported")
            }
        }
    }
    return mask.toULong()
}

@FdflagsType
internal fun posixMaskToFsFdFlags(
    linuxMask: Int,
): Int {
    var flags: Fdflags = 0
    fsFdFlagsMaskToPosixMask.forEach { (openFileFlagsMask, testFlag) ->
        if (testFlag != 0 && linuxMask and testFlag == testFlag) {
            flags = flags or openFileFlagsMask
        }
    }
    return flags
}
