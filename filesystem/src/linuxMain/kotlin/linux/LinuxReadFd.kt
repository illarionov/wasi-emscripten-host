/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ENXIO
import platform.posix.SEEK_CUR
import platform.posix.errno
import platform.posix.iovec
import platform.posix.lseek
import platform.posix.preadv
import platform.posix.readv

internal object LinuxReadFd : FileSystemOperationHandler<ReadFd, ReadError, ULong> {
    override fun invoke(input: ReadFd): Either<ReadError, ULong> {
        return when (input.strategy) {
            CHANGE_POSITION -> callReadWrite(input.fd, input.iovecs) { fd, iovecs, size ->
                readv(fd, iovecs, size)
            }.mapLeft { errNo -> errNo.errnoToReadError(input.fd, input.iovecs) }

            DO_NOT_CHANGE_POSITION -> {
                val currentPosition = lseek(input.fd, 0, SEEK_CUR)
                if (currentPosition < 0) {
                    errno.errnoSeekToReadError(input.fd).left()
                } else {
                    callReadWrite(input.fd, input.iovecs) { fd, iovecs, size ->
                        preadv(fd, iovecs, size, currentPosition)
                    }.mapLeft { errNo -> errNo.errnoToReadError(input.fd, input.iovecs) }
                }
            }
        }
    }

    internal fun callReadWrite(
        @Fd fd: Int,
        iovecs: List<FileSystemByteBuffer>,
        block: (fd: Int, iovecs: CArrayPointer<iovec>, size: Int) -> Long,
    ): Either<Int, ULong> {
        if (iovecs.isEmpty()) {
            return 0UL.right()
        }

        val bytesMoved = memScoped {
            val size = iovecs.size
            val posixIovecs: CArrayPointer<iovec> = allocArray(size)
            iovecs.withPinnedByteArrays { pinnedByteArrays ->
                // TODO: check length
                iovecs.forEachIndexed { index, vec ->
                    posixIovecs[index].apply {
                        iov_base = pinnedByteArrays[index].addressOf(vec.offset)
                        iov_len = vec.length.toULong()
                    }
                }
                block(fd, posixIovecs, size)
            }
        }

        return if (bytesMoved >= 0) {
            bytesMoved.toULong().right()
        } else {
            errno.left()
        }
    }

    private inline fun <R : Any> List<FileSystemByteBuffer>.withPinnedByteArrays(
        block: (byteArrays: List<Pinned<ByteArray>>) -> R,
    ): R {
        val pinnedByteArrays: List<Pinned<ByteArray>> = this.map {
            it.array.pin()
        }
        return try {
            block(pinnedByteArrays)
        } finally {
            pinnedByteArrays.forEach(Pinned<ByteArray>::unpin)
        }
    }

    private fun Int.errnoToReadError(
        @Fd fd: Int,
        iovecs: List<FileSystemByteBuffer>,
    ): ReadError = when (this) {
        EAGAIN -> NotSupported("Non-blocking read would block. Request: `$fd, $iovecs`")
        EBADF -> BadFileDescriptor("Can not read on $fd")
        EINTR -> Interrupted("Read operation interrupted on $fd")
        EINVAL -> InvalidArgument("Invalid argument in request `$fd, $iovecs`")
        EIO -> IoError("I/o error on read from $fd")
        EISDIR -> PathIsDirectory("$fd refers to directory")
        else -> InvalidArgument("Read error. Errno: `$this`")
    }

    private fun Int.errnoSeekToReadError(
        @Fd fd: Int,
    ): ReadError = when (this) {
        EBADF -> BadFileDescriptor("Can not seek on $fd")
        EINVAL -> InvalidArgument("seek() failed. Invalid argument. Fd: $fd")
        ENXIO -> Nxio("trying to seek beyond end of file. Fd: $fd")
        else -> InvalidArgument("Seel failed: unexpected error. Errno: `$this`")
    }
}
