/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.base

import at.released.weh.filesystem.model.Fd
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal class PosixFileSystemState : AutoCloseable {
    private val lock: ReentrantLock = reentrantLock()
    private val openFileDescriptors: MutableSet<@Fd Int> = mutableSetOf()

    fun add(
        @Fd fd: Int,
    ): Unit = lock.withLock {
        val added = openFileDescriptors.add(fd)
        require(added) { "File descriptor $fd already been allocated" }
    }

    fun remove(
        @Fd fd: Int,
    ): Unit = lock.withLock {
        openFileDescriptors.remove(fd)
    }

    override fun close() {
        val fileDescriptors = lock.withLock {
            openFileDescriptors.toList()
        }
        for (fd in fileDescriptors) {
            val errNo = platform.posix.close(fd)
            if (errNo != 0) {
                // close($fd) failed with errno $errNo. Ignore.
            }
        }
    }
}
