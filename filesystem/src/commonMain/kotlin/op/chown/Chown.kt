/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.chown

import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation

public data class Chown(
    val path: String,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,
    public val owner: Int,
    public val group: Int,
    public val followSymlinks: Boolean = true,
) {
    public companion object : FileSystemOperation<Chown, ChownError, Unit> {
        override val tag: String = "chown"
    }
}
