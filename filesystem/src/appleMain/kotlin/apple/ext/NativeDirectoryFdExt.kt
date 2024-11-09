/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.ext

import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.AT_FDCWD

internal val NativeDirectoryFd.posixFd: Int
    get() = if (this == NativeDirectoryFd.CURRENT_WORKING_DIRECTORY) {
        AT_FDCWD
    } else {
        this.raw
    }
