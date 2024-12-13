/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.rename

import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Rename a file or directory
 */
public data class Rename(
    public val oldBaseDirectory: BaseDirectory,
    public val oldPath: VirtualPath,
    public val newBaseDirectory: BaseDirectory,
    public val newPath: VirtualPath,
) {
    public companion object : FileSystemOperation<Rename, RenameError, Unit> {
        override val tag: String = "rename"
    }
}
