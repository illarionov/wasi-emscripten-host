/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readdir

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.model.Filetype

@WasiEmscriptenHostDataModel
public class DirEntry(
    public val name: String,
    public val type: Filetype,
    public val inode: Long,
    public val cookie: Long,
)
