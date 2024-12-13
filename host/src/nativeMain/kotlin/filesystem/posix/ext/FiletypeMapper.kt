/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.ext

import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.Filetype

@FileMode
internal fun posixModeTypeToFilemode(
    linuxModeType: UInt,
): Int {
    return (linuxModeType and 0xfffU).toInt()
}

internal expect fun platformSpecificFiletypeCodeToFiletype(posixType: Int): Filetype

internal fun posixModeTypeToFiletype(
    posixModeType: UInt,
): Filetype {
    // XXX: SOCKET_DGRAM?
    return when (val posixType = posixModeType.toInt() and platform.posix.S_IFMT) {
        platform.posix.S_IFDIR -> Filetype.DIRECTORY
        platform.posix.S_IFCHR -> Filetype.CHARACTER_DEVICE
        platform.posix.S_IFBLK -> Filetype.BLOCK_DEVICE
        platform.posix.S_IFREG -> Filetype.REGULAR_FILE
        platform.posix.S_IFIFO -> Filetype.CHARACTER_DEVICE
        else -> platformSpecificFiletypeCodeToFiletype(posixType)
    }
}
