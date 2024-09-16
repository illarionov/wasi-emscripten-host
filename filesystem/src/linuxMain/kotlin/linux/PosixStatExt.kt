/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import at.released.weh.filesystem.op.stat.FileModeType
import at.released.weh.filesystem.op.stat.FileTypeFlag

@FileModeType
internal fun fileModeTypeFromLinuxModeType(
    linuxModeType: UInt,
): Int {
    val typeMask = when (val posixType = linuxModeType.toInt() and platform.posix.S_IFMT) {
        platform.posix.S_IFDIR -> FileTypeFlag.S_IFDIR
        platform.posix.S_IFCHR -> FileTypeFlag.S_IFCHR
        platform.posix.S_IFBLK -> FileTypeFlag.S_IFBLK
        platform.posix.S_IFREG -> FileTypeFlag.S_IFREG
        platform.posix.S_IFIFO -> FileTypeFlag.S_IFIFO
        platform.posix.S_IFLNK -> FileTypeFlag.S_IFLNK
        platform.posix.S_IFSOCK -> FileTypeFlag.S_IFSOCK
        else -> error("Unexpected type 0x${posixType.toString(16)}")
    }

    val modeMask = linuxModeType and 0xfffU
    return typeMask or modeMask.toInt()
}
