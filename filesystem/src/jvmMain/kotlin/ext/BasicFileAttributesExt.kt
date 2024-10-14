/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import at.released.weh.filesystem.model.Filetype
import java.nio.file.attribute.BasicFileAttributes

internal fun BasicFileAttributes.toFiletype(): Filetype = when {
    this.isRegularFile -> Filetype.REGULAR_FILE
    this.isDirectory -> Filetype.DIRECTORY
    this.isSymbolicLink -> Filetype.SYMBOLIC_LINK
    else -> Filetype.UNKNOWN
}
