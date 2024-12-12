/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.ext

import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.ReparseTag
import platform.windows.FILE_ATTRIBUTE_DIRECTORY

internal fun Filetype.Companion.fromAttributes(
    fileAttributes: FileAttributes,
    reparseTag: ReparseTag,
): Filetype {
    return when {
        fileAttributes.isSymlinkOrReparsePoint && reparseTag.isSymlink -> Filetype.SYMBOLIC_LINK
        fileAttributes.mask.toInt() and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY -> Filetype.DIRECTORY
        else -> Filetype.REGULAR_FILE
    }
}
