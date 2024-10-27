/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import kotlinx.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

internal class NioDirEntrySequence(
    private val rootPath: Path,
    private val stream: DirectoryStream<Path>,
    private val cookie: Long,
) : DirEntrySequence {
    private val instanceLock: Any = Any()
    private var instance: Iterator<DirEntry>? = null
    private val isClosed: AtomicBoolean = AtomicBoolean(false)

    override fun iterator(): Iterator<DirEntry> = synchronized(instanceLock) {
        check(instance == null) { "Iterator should be used only once" }
        val inner = NioDirectoryIterator(rootPath, stream.iterator())
        val withSpecialDirectories = SpecialDirectoryEntriesDecorator(rootPath, inner, isClosed::get)
        val withCookie = OffsetCookieDecorator(withSpecialDirectories, cookie)
        withCookie.also {
            instance = it
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (!isClosed.getAndSet(true)) {
            stream.close()
        }
    }

    override fun toString(): String {
        return "NioDirEntrySequence(root=`$rootPath`)"
    }
}
