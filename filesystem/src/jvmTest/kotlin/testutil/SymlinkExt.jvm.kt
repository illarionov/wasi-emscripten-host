/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import kotlinx.io.files.Path
import java.nio.file.Files
import java.nio.file.Path as NioPath

internal actual fun createSymlink(oldPath: String, newPath: Path) {
    Files.createSymbolicLink(NioPath.of(newPath.toString()), NioPath.of(oldPath))
}
