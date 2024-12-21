/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import kotlinx.io.files.Path

internal expect fun normalizeTargetPath(path: String): String

internal expect fun createSymlink(
    oldPath: String,
    newPath: Path,
    type: SymlinkType = SymlinkType.NOT_SPECIFIED,
)

internal enum class SymlinkType {
    NOT_SPECIFIED,
    SYMLINK_TO_FILE,
    SYMLINK_TO_DIRECTORY,
}
