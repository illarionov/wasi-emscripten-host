/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import arrow.core.getOrElse
import at.released.weh.filesystem.ext.filetype
import at.released.weh.filesystem.ext.readOrGenerateInode
import at.released.weh.filesystem.fdresource.nio.readBasicAttributes
import at.released.weh.filesystem.op.readdir.DirEntry
import java.io.IOException
import java.nio.file.Path

internal fun readDirEntry(
    relativeVirtualPath: String,
    realPath: Path,
    cookie: Long = 0,
): DirEntry {
    val basicAttrs = realPath.readBasicAttributes().getOrElse {
        throw IOException("Can not read file type: $it")
    }
    val inode = realPath.readOrGenerateInode(basicAttrs).getOrElse {
        throw IOException("Can not get inode: $it")
    }

    return DirEntry(
        name = relativeVirtualPath,
        type = basicAttrs.filetype,
        inode = inode,
        cookie = cookie,
    )
}
