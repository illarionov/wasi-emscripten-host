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
import at.released.weh.filesystem.model.Fd

internal class FileDescriptorTable<V : Any> {
    private val fds: MutableMap<@Fd Int, V> = mutableMapOf()

    fun allocate(
        resourceFactory: (@Fd Int) -> V,
    ): Either<Nfile, V> = getFreeFd()
        .map { fd ->
            val channel = resourceFactory(fd)
            val old = fds.put(fd, channel)
            require(old == null) { "File descriptor $fd already been allocated" }
            channel
        }

    operator fun get(@Fd fd: Int): V? = fds[fd]

    fun release(@Fd fd: Int): Either<BadFileDescriptor, V> {
        return fds.remove(fd)?.right() ?: BadFileDescriptor("Trying to remove already disposed file descriptor").left()
    }

    fun drain(): List<V> {
        val values = fds.values.toList()
        fds.clear()
        return values
    }

    private fun getFreeFd(): Either<Nfile, @Fd Int> {
        for (no in MIN_FD..MAX_FD) {
            if (!fds.containsKey(no)) {
                return no.right()
            }
        }
        return Nfile("file descriptor limit exhausted").left()
    }

    companion object {
        const val MIN_FD: Int = 4
        const val MAX_FD: Int = 1024
    }
}
