/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.io.IOException
import kotlinx.io.files.Path
import platform.posix.errno
import platform.posix.strerror
import platform.posix.symlink

internal actual fun createSymlink(oldPath: String, newPath: Path) {
    if (symlink(oldPath, newPath.toString()) < 0) {
        throw IOException(
            "Failed to create symlink `$oldPath` to `$newPath`: `${strerror(errno)?.toKStringFromUtf8()}`",
        )
    }
}
