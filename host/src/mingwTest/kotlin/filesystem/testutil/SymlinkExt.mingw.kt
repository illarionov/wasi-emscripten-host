/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.weh.filesystem.testutil.SymlinkType.NOT_SPECIFIED
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_DIRECTORY
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_FILE
import kotlinx.io.files.Path
import platform.windows.CreateSymbolicLinkW
import platform.windows.GetLastError
import platform.windows.SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE
import platform.windows.SYMBOLIC_LINK_FLAG_DIRECTORY

actual fun normalizeTargetPath(path: String): String = path.replace('/', '\\')

internal actual fun createSymlink(oldPath: String, newPath: Path, type: SymlinkType) {
    val flags = type.mask or SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE.toUInt()

    if (CreateSymbolicLinkW(newPath.toString(), oldPath, flags).toInt() == 0) {
        val lastError = GetLastError()
        throw WindowsIoException(
            "Can not create symlink `$newPath` to `$oldPath`: error 0x${lastError.toString(16)}",
            lastError,
        )
    }
}

private val SymlinkType.mask: UInt
    get() = when (this) {
        NOT_SPECIFIED, SYMLINK_TO_FILE -> 0
        SYMLINK_TO_DIRECTORY -> SYMBOLIC_LINK_FLAG_DIRECTORY
    }.toUInt()
