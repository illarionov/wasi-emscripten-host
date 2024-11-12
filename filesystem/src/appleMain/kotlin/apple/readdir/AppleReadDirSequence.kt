/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.readdir

import at.released.weh.filesystem.internal.op.readdir.OffsetCookieDecorator
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Cookie
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Start
import at.released.weh.filesystem.posix.readdir.PosixDirectoryIterator
import at.released.weh.filesystem.posix.readdir.posixReadDir
import at.released.weh.filesystem.preopened.VirtualPath
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.CPointer
import kotlinx.io.IOException
import platform.posix.DIR
import platform.posix.closedir
import platform.posix.errno
import platform.posix.rewinddir
import platform.posix.strerror

internal class AppleReadDirSequence(
    private val virtualPath: VirtualPath,
    private val dir: CPointer<DIR>,
    private val startPosition: DirSequenceStartPosition,
) : DirEntrySequence {
    private val isClosed: AtomicBoolean = atomic(false)
    private var instance: Iterator<DirEntry>? = null

    override fun iterator(): Iterator<DirEntry> {
        check(instance == null) { "Iterator should be used only once" }

        rewinddir(dir)
        val firstEntry = posixReadDir(dir)
        val innerIterator = PosixDirectoryIterator(firstEntry, isClosed::value) { posixReadDir(dir) }
        val cookie = when (startPosition) {
            Start -> 0
            is Cookie -> startPosition.cookie
        }

        // We use own dummy cookies instead of using telldir() because cookies from telldir() are valid only within
        // the same opened descriptor DIR.
        val withCookie = OffsetCookieDecorator(innerIterator, cookie)
        instance = withCookie
        return withCookie
    }

    override fun close() {
        if (!isClosed.getAndSet(true)) {
            val resultCode = closedir(dir)
            if (resultCode != 0) {
                val err = errno
                throw IOException("Can not close directory: $err (${strerror(err)})")
            }
        }
    }

    override fun toString(): String {
        return "AppleDirEntrySequence($virtualPath, dir=$dir, startPosition: $startPosition, isClosed=$isClosed)"
    }
}
