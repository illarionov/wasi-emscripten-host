/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

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
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.posix.NativeFd
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointer
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

internal fun posixReadChangePosition(
    nativeFd: NativeFd,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer: CPointer<iovec>, size ->
        readv(fd.fd, iovecsPointer, size)
    }.mapLeft { errNo -> errNo.errnoToReadError(nativeFd, iovecs) }
}

internal fun posixReadDoNotChangePosition(
    nativeFd: NativeFd,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    val currentPosition = lseek(nativeFd.fd, 0, SEEK_CUR)
    return if (currentPosition < 0) {
        errno.errnoSeekToReadError(nativeFd).left()
    } else {
        callReadWrite(nativeFd, iovecs) { fd, iovecsPointer, size ->
            preadv(fd.fd, iovecsPointer, size, currentPosition)
        }.mapLeft { errNo -> errNo.errnoToReadError(nativeFd, iovecs) }
    }
}

internal fun callReadWrite(
    fd: NativeFd,
    iovecs: List<FileSystemByteBuffer>,
    block: (fd: NativeFd, iovecs: CArrayPointer<iovec>, size: Int) -> Long,
): Either<Int, ULong> {
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
    fd: NativeFd,
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
    fd: NativeFd,
): ReadError = when (this) {
    EBADF -> BadFileDescriptor("Can not seek on $fd")
    EINVAL -> InvalidArgument("seek() failed. Invalid argument. Fd: $fd")
    ENXIO -> Nxio("trying to seek beyond end of file. Fd: $fd")
    else -> InvalidArgument("Seel failed: unexpected error. Errno: `$this`")
}
