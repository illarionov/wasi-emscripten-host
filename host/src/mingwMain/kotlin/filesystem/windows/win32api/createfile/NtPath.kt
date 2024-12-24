/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.createfile

import at.released.weh.filesystem.path.real.windows.nt.WindowsNtObjectManagerPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import platform.windows.HANDLE

internal sealed class NtPath {
    abstract val handle: HANDLE?
    abstract val pathString: String

    internal data class Relative(
        override val handle: HANDLE,
        val path: WindowsNtRelativePath = WindowsNtRelativePath.CURRENT,
    ) : NtPath() {
        override val pathString: String get() = path.kString
    }

    internal data class Absolute(
        val path: WindowsNtObjectManagerPath,
    ) : NtPath() {
        override val handle: HANDLE? get() = null
        override val pathString: String get() = path.kString
    }
}
