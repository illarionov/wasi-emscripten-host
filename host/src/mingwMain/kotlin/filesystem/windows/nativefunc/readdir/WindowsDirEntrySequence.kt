/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readdir

import at.released.weh.filesystem.internal.op.readdir.OffsetCookieDecorator
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Cookie
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Start
import at.released.weh.filesystem.posix.readdir.PosixDirectoryIterator
import platform.windows.HANDLE

internal class WindowsDirEntrySequence(
    private val rootHandle: HANDLE,
    private val startPosition: DirSequenceStartPosition,
) : DirEntrySequence {
    private var isClosed: Boolean = false
    private var instance: Iterator<DirEntry>? = null
    private var nextEntryProvider: WindowsNextEntryProvider? = null

    override fun iterator(): Iterator<DirEntry> {
        check(instance == null) { "Iterator should be used only once" }

        val (firstEntry, nextEntryProvider) = WindowsNextEntryProvider.create(rootHandle)

        val innerIterator = PosixDirectoryIterator(firstEntry, ::isClosed) {
            nextEntryProvider.readNextDir()
        }

        val cookie = when (startPosition) {
            Start -> 0
            is Cookie -> startPosition.cookie
        }
        val withCookie = OffsetCookieDecorator(innerIterator, cookie)

        this.instance = withCookie
        this.nextEntryProvider = nextEntryProvider
        return withCookie
    }

    override fun close() {
        isClosed = true
        nextEntryProvider?.close()
    }

    override fun toString(): String {
        return "WindowsDirEntrySequence($rootHandle, $startPosition, isClosed=$isClosed)"
    }
}
