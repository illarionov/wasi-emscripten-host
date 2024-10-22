/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import at.released.weh.filesystem.op.readdir.DirEntry
import java.nio.file.Path

/**
 * Adds special `.` and `..` directory entries to the sequence.
 * These entries are only required in WASI Preview 1 and are no longer required in preview 2.
 */
@Suppress("IteratorNotThrowingNoSuchElementException")
internal class SpecialDirectoryEntriesDecorator(
    private val rootPath: Path,
    private val delegate: Iterator<DirEntry>,
    private val streamIsClosed: () -> Boolean,
    private val dirEntryReader: (name: String, realPath: Path, cookie: Long) -> DirEntry = ::readDirEntry,
) : Iterator<DirEntry> {
    private var specialDirPosition: Int = 0

    override fun hasNext(): Boolean {
        return if (specialDirPosition < 2) {
            true
        } else {
            return delegate.hasNext()
        }
    }

    override fun next(): DirEntry {
        return if (specialDirPosition < 2) {
            nextSpecialEntry()
        } else {
            delegate.next()
        }
    }

    private fun nextSpecialEntry(): DirEntry {
        check(!streamIsClosed()) { "Stream is closed" }

        val current = specialDirPosition
        specialDirPosition += 1

        return when (current) {
            0 -> dirEntryReader(".", rootPath, 0)
            // XXX Should root paths not available from the virtual file system be hidden?
            1 -> dirEntryReader("..", rootPath.resolve(".."), 0)
            else -> error("Incorrect special dir index $current")
        }
    }
}
