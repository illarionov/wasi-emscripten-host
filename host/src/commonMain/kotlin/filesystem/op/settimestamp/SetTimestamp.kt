/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.settimestamp

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

@WasiEmscriptenHostDataModel
public class SetTimestamp(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,
    public val atimeNanoseconds: Long?,
    public val mtimeNanoseconds: Long?,
    public val followSymlinks: Boolean,
) {
   public companion object : FileSystemOperation<SetTimestamp, SetTimestampError, Unit> {
       override val tag: String = "setts"
   }
}
