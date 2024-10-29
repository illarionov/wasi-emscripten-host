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
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.Position
import at.released.weh.filesystem.posix.NativeFileFd
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
import platform.posix.NULL
import platform.posix.errno
import platform.posix.iovec
import platform.posix.preadv
import platform.posix.readv

internal fun posixRead(
    nativeChannel: NativeFileChannel,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = posixRead(nativeChannel.fd, iovecs, strategy)

private fun posixRead(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = when (strategy) {
    CurrentPosition -> posixReadChangePosition(nativeFd, iovecs)
    is Position -> posixReadDoNotChangePosition(nativeFd, strategy.position, iovecs)
}

private fun posixReadChangePosition(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer: CPointer<iovec>, size ->
        readv(fd.fd, iovecsPointer, size)
    }.mapLeft { errNo -> errNo.errnoToReadError(nativeFd, iovecs) }
}

private fun posixReadDoNotChangePosition(
    nativeFd: NativeFileFd,
    offset: Long,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer, size ->
        preadv(fd.fd, iovecsPointer, size, offset)
    }.mapLeft { errNo -> errNo.errnoToReadError(nativeFd, iovecs) }
}

internal fun callReadWrite(
    fd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
    block: (fd: NativeFileFd, iovecs: CArrayPointer<iovec>, size: Int) -> Long,
): Either<Int, ULong> {
    val bytesMoved = memScoped {
        val size = iovecs.size
        val posixIovecs: CArrayPointer<iovec> = allocArray(size)
        iovecs.withPinnedByteArrays { pinnedByteArrays: List<Pinned<ByteArray>?> ->
            iovecs.forEachIndexed { index, vec ->
                posixIovecs[index].apply {
                    val pinnedByteArray = pinnedByteArrays[index]
                    if (pinnedByteArray != null) {
                        iov_base = pinnedByteArray.addressOf(vec.offset)
                        iov_len = vec.length.toULong()
                    } else {
                        iov_base = NULL
                        iov_len = 0U
                    }
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
    block: (byteArrays: List<Pinned<ByteArray>?>) -> R,
): R {
    val pinnedByteArrays: List<Pinned<ByteArray>?> = this.map {
        @Suppress("ReplaceSizeCheckWithIsNotEmpty")
        if (it.array.size != 0) {
            it.array.pin()
        } else {
            null
        }
    }
    return try {
        block(pinnedByteArrays)
    } finally {
        pinnedByteArrays.filterNotNull().forEach(Pinned<ByteArray>::unpin)
    }
}

private fun Int.errnoToReadError(
    fd: NativeFileFd,
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
