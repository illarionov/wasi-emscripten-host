/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
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
import at.released.weh.filesystem.posix.nativefunc.callReadWrite
import kotlinx.cinterop.CPointer
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.iovec
import platform.posix.preadv
import platform.posix.readv

internal fun linuxRead(
    nativeChannel: NativeFileChannel,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = linuxRead(nativeChannel.fd, iovecs, strategy)

private fun linuxRead(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> = when (strategy) {
    CurrentPosition -> linuxReadChangePosition(nativeFd, iovecs)
    is Position -> linuxReadDoNotChangePosition(nativeFd, strategy.position, iovecs)
}

private fun linuxReadChangePosition(
    nativeFd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer: CPointer<iovec>, size ->
        readv(fd.fd, iovecsPointer, size)
    }.mapLeft { errNo -> errNo.linuxPreadErrnoToReadError(nativeFd, iovecs) }
}

private fun linuxReadDoNotChangePosition(
    nativeFd: NativeFileFd,
    offset: Long,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> {
    return callReadWrite(nativeFd, iovecs) { fd, iovecsPointer, size ->
        preadv(fd.fd, iovecsPointer, size, offset)
    }.mapLeft { errNo -> errNo.linuxPreadErrnoToReadError(nativeFd, iovecs) }
}

private fun Int.linuxPreadErrnoToReadError(
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
