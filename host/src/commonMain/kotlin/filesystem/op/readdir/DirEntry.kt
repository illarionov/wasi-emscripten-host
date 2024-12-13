/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readdir

import at.released.weh.filesystem.model.Filetype

public data class DirEntry(
    val name: String,
    val type: Filetype,
    val inode: Long,
    val cookie: Long,
)
