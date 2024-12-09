/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.symlink

import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Creates a symbolic link.
 * [oldPath] is a contents of the symbolic link.
 * [newPath] is a destination path of the symbolic link, resolved relative to the [newPathBaseDirectory].
 */
public data class Symlink(
    public val oldPath: VirtualPath,
    public val newPath: VirtualPath,
    public val newPathBaseDirectory: BaseDirectory = CurrentWorkingDirectory,
    public val allowAbsoluteOldPath: Boolean = false,
) {
    public companion object : FileSystemOperation<Symlink, SymlinkError, Unit> {
        override val tag: String = "symlink"
    }
}
