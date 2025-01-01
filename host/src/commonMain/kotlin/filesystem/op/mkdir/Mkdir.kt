/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.mkdir

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

@WasiEmscriptenHostDataModel
public class Mkdir(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,

    @FileMode
    public val mode: Int,
    public val failIfExists: Boolean = true,
) {
    override fun toString(): String {
        return "Mkdir(" +
                "path='$path', " +
                "baseDirectory=$baseDirectory, " +
                "mode=0${mode.toString(8)}, " +
                "failIfExists=$failIfExists)"
    }

    public companion object : FileSystemOperation<Mkdir, MkdirError, Unit> {
        override val tag: String = "mkdir"
    }
}
