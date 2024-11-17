/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import platform.windows.HANDLE

internal sealed class FileDirectoryHandle {
    class File(
        val handle: HANDLE,
        val isInAppendMode: Boolean,
    ) : FileDirectoryHandle()

    class Directory(
        val handle: HANDLE,
    ) : FileDirectoryHandle()
}
