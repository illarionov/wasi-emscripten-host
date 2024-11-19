/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.ReparseTag

internal data class FileAttributeTagInfo(
    val fileAttributes: FileAttributes,
    val reparseTag: ReparseTag,
) {
    val isSymlink = fileAttributes.isSymlinkOrReparsePoint && reparseTag.isSymlink
}
