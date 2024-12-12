/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import at.released.weh.filesystem.op.readdir.DirEntry
import java.nio.file.Path

@Suppress("IteratorNotThrowingNoSuchElementException")
internal class NioDirectoryIterator(
    private val rootPath: Path,
    private val directoryIterator: Iterator<Path>,
    private val dirEntryReader: (name: String, realPath: Path, cookie: Long) -> DirEntry = ::readDirEntry,
) : Iterator<DirEntry> {
    override fun hasNext(): Boolean {
        return directoryIterator.hasNext()
    }

    override fun next(): DirEntry {
        val nextPath = directoryIterator.next()
        val relativePath = if (nextPath.isAbsolute) {
            rootPath.relativize(nextPath)
        } else {
            nextPath
        }.toString()
        return dirEntryReader(relativePath, nextPath, 0)
    }
}
