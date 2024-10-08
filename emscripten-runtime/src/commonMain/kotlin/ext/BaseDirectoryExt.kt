/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.ext

import at.released.weh.emcripten.runtime.include.Fcntl
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory

internal fun BaseDirectory.Companion.fromRawDirFd(rawDirFd: Int): BaseDirectory = when (rawDirFd) {
    Fcntl.AT_FDCWD -> CurrentWorkingDirectory
    else -> BaseDirectory.DirectoryFd(rawDirFd)
}
