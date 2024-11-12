/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.Position
import at.released.weh.filesystem.platform.apple.preadv
import at.released.weh.filesystem.platform.apple.readv
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.nativefunc.callReadWrite
import kotlinx.cinterop.CPointer
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.ECONNRESET
import platform.posix.EDEADLK
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ENOBUFS
import platform.posix.ENOTCONN
import platform.posix.ENXIO
import platform.posix.ESPIPE
import platform.posix.ESTALE
import platform.posix.ETIMEDOUT
import platform.posix.iovec

internal fun appleRead(
    nativeChannel: NativeFileChannel,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = appleRead(nativeChannel.fd, iovecs, strategy)

private fun appleRead(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = when (strategy) {
    CurrentPosition -> appleReadChangePosition(nativeFd, iovecs)
    is Position -> appleReadDoNotChangePosition(nativeFd, strategy.position, iovecs)
}

private fun appleReadChangePosition(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer: CPointer<iovec>, size ->
        readv(fd.fd, iovecsPointer, size)
    }.mapLeft { errNo -> errNo.applePreadErrnoToReadError(nativeFd, iovecs) }
}

private fun appleReadDoNotChangePosition(
    nativeFd: NativeFileFd,
    offset: Long,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer, size ->
        preadv(fd.fd, iovecsPointer, size, offset)
    }.mapLeft { errNo -> errNo.applePreadErrnoToReadError(nativeFd, iovecs) }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.applePreadErrnoToReadError(
    fd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
): ReadError = when (this) {
    EAGAIN -> NotSupported("Non-blocking read would block. Request: `$fd, $iovecs`")
    EBADF -> BadFileDescriptor("Can not read on $fd")
    ECONNRESET -> IoError("Connection is closed by the peer")
    EDEADLK -> IoError("Cannot materialize dataless file")
    EINTR -> Interrupted("Read operation interrupted on $fd")
    EINVAL -> InvalidArgument("Invalid argument in request `$fd, $iovecs`")
    EIO -> IoError("I/o error on read from $fd")
    EISDIR -> PathIsDirectory("$fd refers to directory")
    ENOBUFS -> IoError("Can not allocate memory buffer")
    ENOTCONN -> IoError("Attempt to red on an unconnected socket")
    ENXIO -> BadFileDescriptor("Action cannot be performed by the device")
    ESPIPE -> BadFileDescriptor("File descriptor associated with a pipe, socket or FIFO")
    ESTALE -> IoError("Cannot read: file already deleted")
    ETIMEDOUT -> IoError("Timeout while reading remote file")
    else -> InvalidArgument("Read error. Errno: `$this`")
}
