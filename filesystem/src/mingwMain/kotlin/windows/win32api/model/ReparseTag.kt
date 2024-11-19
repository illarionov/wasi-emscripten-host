/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.model

import platform.windows.IO_REPARSE_TAG_SYMLINK

internal value class ReparseTag(
    val code: UInt
) {
    val isSymlink: Boolean get() = code == IO_REPARSE_TAG_SYMLINK
}
