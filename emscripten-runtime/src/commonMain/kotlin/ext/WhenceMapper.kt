/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.ext

import at.released.weh.filesystem.model.Whence as FileSystemWhence

internal object WhenceMapper {
    internal fun fromEmscriptenIdOrNull(
        id: Int,
    ): FileSystemWhence? = when (id) {
        0 -> FileSystemWhence.SET
        1 -> FileSystemWhence.CUR
        2 -> FileSystemWhence.END
        else -> null
    }
}
