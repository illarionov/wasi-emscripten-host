/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.readdir

import at.released.weh.filesystem.linux.readdir.ReadDirResult.EndOfStream
import at.released.weh.filesystem.linux.readdir.ReadDirResult.Entry
import at.released.weh.filesystem.linux.readdir.ReadDirResult.Error
import at.released.weh.filesystem.op.readdir.DirEntry
import kotlinx.io.IOException

internal class LinuxDirectoryIterator(
    private var next: ReadDirResult,
    private val streamIsClosed: () -> Boolean,
    private val nextDirProvider: () -> ReadDirResult,
) : Iterator<DirEntry> {
    override fun hasNext(): Boolean {
        return next != EndOfStream
    }

    override fun next(): DirEntry {
        check(!streamIsClosed()) { "Stream is closed" }

        when (val current: ReadDirResult = next) {
            EndOfStream -> throw NoSuchElementException()

            is Entry -> {
                next = nextDirProvider()
                return current.entry
            }

            is Error -> {
                next = EndOfStream
                throw IOException("Can not read directory: ${current.error}")
            }
        }
    }
}
