/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import at.released.weh.filesystem.op.stat.FileModeType

internal fun FileModeType.Companion.fromLinuxModeType(
    linuxModeType: UInt,
): FileModeType {
    var type = linuxModeType.toInt() and platform.posix.S_IFMT
    val typeMask = when (type) {
        platform.posix.S_IFDIR -> FileModeType.S_IFDIR
        platform.posix.S_IFCHR -> FileModeType.S_IFCHR
        platform.posix.S_IFBLK -> FileModeType.S_IFBLK
        platform.posix.S_IFREG -> FileModeType.S_IFREG
        platform.posix.S_IFIFO -> FileModeType.S_IFIFO
        platform.posix.S_IFLNK -> FileModeType.S_IFLNK
        platform.posix.S_IFSOCK -> FileModeType.S_IFSOCK
        else -> error("Unexpected type 0x${type.toString(16)}")
    }

    val modeMask = linuxModeType and 0xfffU
    return FileModeType(typeMask or modeMask)
}
