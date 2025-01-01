/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.unlink

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

/**
 * Remove a file or symbolic link at [path] relative to the base directory [baseDirectory].
 */
@WasiEmscriptenHostDataModel
public class UnlinkFile(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,
) {
    public companion object : FileSystemOperation<UnlinkFile, UnlinkError, Unit> {
        override val tag: String = "unlinkfile"
    }
}
