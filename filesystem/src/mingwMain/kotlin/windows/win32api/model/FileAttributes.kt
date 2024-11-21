/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.model

import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT

internal value class FileAttributes(
    val mask: UInt,
) {
    val isSymlinkOrReparsePoint: Boolean
        get() = mask.toInt() and FILE_ATTRIBUTE_REPARSE_POINT == FILE_ATTRIBUTE_REPARSE_POINT

    val isDirectory: Boolean get() = mask.toInt() and FILE_ATTRIBUTE_DIRECTORY == FILE_ATTRIBUTE_DIRECTORY
}
