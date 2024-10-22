/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import arrow.core.getOrElse
import at.released.weh.filesystem.fdresource.nio.readFileType
import at.released.weh.filesystem.fdresource.nio.readInode
import at.released.weh.filesystem.op.readdir.DirEntry
import kotlinx.io.IOException
import java.nio.file.Path

internal fun readDirEntry(
    relativeVirtualPath: String,
    realPath: Path,
    cookie: Long = 0,
): DirEntry {
    val fileType = realPath.readFileType().getOrElse { throw IOException("Can not read file type: $it") }
    val inode = realPath.readInode().getOrElse { 0 }
    return DirEntry(
        name = relativeVirtualPath,
        type = fileType,
        inode = inode,
        cookie = cookie,
    )
}
