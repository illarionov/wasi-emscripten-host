/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor

internal class FileDescriptorTable<V : Any>(
    initial: Map<FileDescriptor, V> = emptyMap(),
) {
    private val fds: MutableMap<FileDescriptor, V> = LinkedHashMap(initial)

    operator fun set(
        descriptor: FileDescriptor,
        handle: V,
    ) {
        require(!fds.containsKey(descriptor)) {
            "$descriptor already set"
        }
        fds[descriptor] = handle
    }

    fun <R : V> allocate(
        resourceFactory: (FileDescriptor) -> R,
    ): Either<Nfile, Pair<FileDescriptor, R>> = getFreeFd()
        .map { fd ->
            val channel = resourceFactory(fd)
            val old = fds.put(fd, channel)
            require(old == null) { "File descriptor $fd already been allocated" }
            fd to channel
        }

    operator fun get(@IntFileDescriptor fd: FileDescriptor): V? = fds[fd]

    fun release(@IntFileDescriptor fd: FileDescriptor): Either<BadFileDescriptor, V> {
        return fds.remove(fd)?.right() ?: BadFileDescriptor("Trying to remove already disposed file descriptor").left()
    }

    fun drain(): List<V> {
        val values = fds.values.toList()
        fds.clear()
        return values
    }

    private fun getFreeFd(): Either<Nfile, FileDescriptor> {
        for (no in MIN_FD..MAX_FD) {
            if (!fds.containsKey(no)) {
                return no.right()
            }
        }
        return Nfile("file descriptor limit exhausted").left()
    }

    companion object {
        const val WASI_STDIN_FD: Int = 0
        const val WASI_STDOUT_FD: Int = 1
        const val WASI_STDERR_FD: Int = 2
        const val WASI_FIRST_PREOPEN_FD: Int = 3
        const val MIN_FD: Int = 5
        const val MAX_FD: Int = 1024
        const val INVALID_FD: FileDescriptor = -1
    }
}
