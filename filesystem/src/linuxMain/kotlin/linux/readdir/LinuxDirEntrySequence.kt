/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.readdir

import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Cookie
import at.released.weh.filesystem.preopened.VirtualPath
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.CPointer
import kotlinx.io.IOException
import platform.posix.DIR
import platform.posix.closedir
import platform.posix.errno
import platform.posix.seekdir
import platform.posix.strerror

internal class LinuxDirEntrySequence(
    private val virtualPath: VirtualPath,
    private val dir: CPointer<DIR>,
    private val startPosition: DirSequenceStartPosition,
) : DirEntrySequence {
    private val isClosed: AtomicBoolean = atomic(false)

    override fun iterator(): Iterator<DirEntry> {
        if (startPosition is Cookie) {
            // XXX: location returned by the telldir is valid only within the same opened descriptor DIR.
            seekdir(dir, startPosition.cookie)
        }
        val firstEntry = linuxReadDir(dir)
        return LinuxDirectoryIterator(firstEntry, isClosed::value) {
            linuxReadDir(dir)
        }
    }

    override fun close() {
        isClosed.value = true
        val resultCode = closedir(dir)
        if (resultCode != 0) {
            val err = errno
            throw IOException("Can not close directory: $err (${strerror(err)})")
        }
    }

    override fun toString(): String {
        return "LinuxDirEntrySequence($virtualPath, dir=$dir, isClosed=$isClosed)"
    }
}
