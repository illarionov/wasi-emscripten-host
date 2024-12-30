/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import arrow.core.Either
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.stdio.StdioPollEvent
import at.released.weh.filesystem.stdio.StdioSource
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.IOException
import platform.posix.EBADF
import platform.posix.STDIN_FILENO
import platform.posix.dup
import platform.posix.errno

internal expect fun readNative(
    fd: Int,
    buf: CValuesRef<*>,
    count: Int,
): Either<Int, Int>

internal class PosixFdSource private constructor(
    private val fd: FileDescriptor,
) : StdioSource {
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    private var isClosed = atomic<Boolean>(false)

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        checkSourceNotClosed()
        require(byteCount >= 0)
        val byteArray = ByteArray(byteCount.toInt().coerceAtMost(MAX_REQUEST_BYTES))
        val bytesOrError = byteArray.usePinned {
            readNative(fd, it.addressOf(0), it.get().size)
        }
        return bytesOrError.fold(
            ifRight = { bytesRead ->
                if (bytesRead == 0) {
                    -1L
                } else {
                    sink.write(byteArray, bytesRead)
                    bytesRead.toLong()
                }
            },
            ifLeft = { errNo ->
                @Suppress("UseCheckOrError")
                when (errNo) {
                    EBADF -> throw IllegalStateException("Bad file descriptor")
                    else -> throw IOException("Error $errno")
                }
            },
        )
    }

    override fun pollNonblocking(): Either<NonblockingPollError, StdioPollEvent> {
        // TODO
        return super.pollNonblocking()
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

    private fun checkSourceNotClosed(): Unit = check(!isClosed.value) { "Source is closed" }

    internal companion object {
        const val MAX_REQUEST_BYTES = 65536

        fun create(
            fd: FileDescriptor = STDIN_FILENO,
        ): PosixFdSource {
            val newfd = dup(fd)
            if (newfd == -1) {
                throw IOException("Can not duplicate $fd. Error `$errno`")
            }
            return PosixFdSource(newfd)
        }
    }
}
