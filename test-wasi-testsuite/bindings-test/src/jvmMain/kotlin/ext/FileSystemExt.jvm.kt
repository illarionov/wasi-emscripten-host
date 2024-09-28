/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

internal actual fun FileSystem.setCurrentWorkingDirectory(path: Path) {
    // XXX: rewrite, ignore for now
}
