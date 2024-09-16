/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.host.include.Fcntl

@InternalWasiEmscriptenHostApi
public fun BaseDirectory.Companion.fromRawDirFd(rawDirFd: Int): BaseDirectory = when (rawDirFd) {
    Fcntl.AT_FDCWD -> CurrentWorkingDirectory
    else -> BaseDirectory.DirectoryFd(rawDirFd)
}
