/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.ext

import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.BaseDirectory.None
import at.released.weh.filesystem.platform.linux.AT_FDCWD

internal fun BaseDirectory.toDirFd(): Int = when (this) {
    None -> AT_FDCWD
    CurrentWorkingDirectory -> AT_FDCWD
    is DirectoryFd -> this.fd.fd
}
