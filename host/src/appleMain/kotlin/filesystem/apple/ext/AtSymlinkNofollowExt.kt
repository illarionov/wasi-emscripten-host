/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.ext

import platform.posix.AT_SYMLINK_NOFOLLOW

internal fun followSymlinksAsAtSymlinkFlags(
    followSymlinks: Boolean,
): Int = if (followSymlinks) {
    0
} else {
    AT_SYMLINK_NOFOLLOW
}
