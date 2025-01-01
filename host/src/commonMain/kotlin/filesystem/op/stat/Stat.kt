/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Retrieves information about the file at the given [path].
 * If [path] is relative, it will be resolved using the [baseDirectory] provided.
 *
 * By default, all symbolic links are followed. If [followSymlinks] is set to false and [path] is a symbolic link,
 * the status of the symbolic link itself is returned instead of the target file.
 */
@WasiEmscriptenHostDataModel
public class Stat(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,
    public val followSymlinks: Boolean = true,
) {
    public companion object : FileSystemOperation<Stat, StatError, StructStat> {
        override val tag: String = "stat"
    }
}
