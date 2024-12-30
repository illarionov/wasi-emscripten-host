/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.stdio.StdioPollEvent
import at.released.weh.filesystem.stdio.StdioSink
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray
import platform.posix.STDOUT_FILENO
import platform.posix.dup
import platform.posix.errno

internal expect fun syncNative(
    fd: Int,
): Either<Int, Unit>

internal expect fun writeNative(
    fd: Int,
    buf: CValuesRef<*>,
    bytes: Int,
): Either<Int, Int>

internal class PosixFdSink private constructor(
    private val fd: FileDescriptor,
) : StdioSink {
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    private var isClosed = atomic<Boolean>(false)

    override fun flush() {
        checkSinkNotClosed()
        syncNative(fd)
            .onLeft { errno ->
                throw IOException("Can not flush $fd: $errno")
            }
    }

    override fun write(source: Buffer, byteCount: Long) {
        checkSinkNotClosed()
        val bytes = source.readByteArray(byteCount.toInt())
        val bytesOrError = bytes.usePinned { buf ->
            val totalBytes = byteCount.toInt()
            var offset = 0
            while (offset != totalBytes) {
                val writtenBytes = writeNative(fd, buf.addressOf(offset), totalBytes - offset)
                    .getOrElse {
                        @Suppress("CUSTOM_LABEL")
                        return@usePinned it.right()
                    }
                offset += writtenBytes
            }
            offset.left()
        }
        bytesOrError.onRight {
            throw IOException("Can not write to $fd: $errno")
        }
    }

    override fun close() {
        if (isClosed.getAndSet(true)) {
            // Do not close the same file descriptor twice even in case of an error
            return
        }

        val result = platform.posix.close(fd)
        if (result == -1) {
            throw IOException("Can not close $fd. Error `$errno`")
        }
    }

    override fun pollNonblocking(): Either<NonblockingPollError, StdioPollEvent> {
        // TODO
        return super.pollNonblocking()
    }

    private fun checkSinkNotClosed(): Unit = check(!isClosed.value) { "Sink is closed" }

    internal companion object {
        fun create(
            fd: FileDescriptor = STDOUT_FILENO,
        ): PosixFdSink {
            val newfd = dup(fd)
            if (newfd == -1) {
                throw IOException("Can not duplicate $fd. Error `$errno`")
            }
            return PosixFdSink(newfd)
        }
    }
}
