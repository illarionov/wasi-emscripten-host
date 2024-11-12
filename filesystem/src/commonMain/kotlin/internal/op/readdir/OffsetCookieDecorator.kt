/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.op.readdir

import at.released.weh.filesystem.op.readdir.DirEntry
import kotlinx.io.IOException

/**
 * Adds offset as a directory cookie.
 *
 * Dirty incorrect workaround for systems where system cookies are not available.
 */
internal class OffsetCookieDecorator(
    private val delegate: Iterator<DirEntry>,
    private val startOffset: Long = 0,
) : Iterator<DirEntry> {
    private var currentOffset: Long = 0
    private var isComplete: Boolean = false

    override fun hasNext(): Boolean {
        flushStart()
        if (isComplete) {
            return false
        }
        return delegate.hasNext()
    }

    override fun next(): DirEntry {
        flushStart()
        if (isComplete) {
            throw NoSuchElementException()
        }

        currentOffset += 1
        val next = delegate.next().copy(cookie = currentOffset)

        return next
    }

    @Suppress("SwallowedException")
    private fun flushStart() {
        while (currentOffset < startOffset && !isComplete) {
            try {
                if (delegate.hasNext()) {
                    delegate.next()
                    currentOffset += 1
                } else {
                    isComplete = true
                }
            } catch (nse: NoSuchElementException) {
                isComplete = true
            } catch (ioe: IOException) {
                isComplete = true
            }
        }
    }
}
