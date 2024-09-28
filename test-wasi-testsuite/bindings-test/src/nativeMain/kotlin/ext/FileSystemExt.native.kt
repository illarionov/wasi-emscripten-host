/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import kotlinx.cinterop.toKString
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import platform.posix.chdir
import platform.posix.strerror

internal actual fun FileSystem.setCurrentWorkingDirectory(path: Path) {
    val absolutePath = this.resolve(path).toString()
    val errno = chdir(absolutePath)
    if (errno != 0) {
        error("chdir() failed: $errno (${strerror(errno)?.toKString()})")
    }
}
