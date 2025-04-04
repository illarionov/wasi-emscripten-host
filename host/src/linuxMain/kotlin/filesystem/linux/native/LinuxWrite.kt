/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.FileTooBig
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.Overflow
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.Position
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.nativefunc.callReadWrite
import at.released.weh.filesystem.posix.nativefunc.posixSeek
import kotlinx.cinterop.CArrayPointer
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ENOSPC
import platform.posix.EPERM
import platform.posix.EPIPE
import platform.posix.iovec
import platform.posix.pwritev
import platform.posix.writev

internal fun linuxWrite(
    nativeChannel: NativeFileChannel,
    cIovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
) = linuxWrite(nativeChannel.fd, nativeChannel.isInAppendMode, cIovecs, strategy)

private fun linuxWrite(
    nativeFd: NativeFileFd,
    isInAppendMode: Boolean,
    cIovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
) = when (strategy) {
    CurrentPosition -> linuxWriteChangePosition(nativeFd, isInAppendMode, cIovecs)
    is Position -> linuxWriteDoNotChangePosition(nativeFd, strategy.position, cIovecs)
}

private fun linuxWriteChangePosition(
    nativeFd: NativeFileFd,
    isInAppendMode: Boolean,
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> {
    // XXX seek and write should be atomic
    if (isInAppendMode) {
        posixSeek(nativeFd, 0, Whence.END)
            .mapLeft(SeekError::toWriteError)
            .onLeft { return it.left() }
    }

    return callReadWrite(nativeFd, cIovecs) { fd: NativeFileFd, iovecs: CArrayPointer<iovec>, size: Int ->
        writev(fd.fd, iovecs, size)
    }.mapLeft { errNo -> errNo.writevErrnoToWriteError(nativeFd, cIovecs) }
}

private fun linuxWriteDoNotChangePosition(
    nativeFd: NativeFileFd,
    position: Long,
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> {
    return callReadWrite(nativeFd, cIovecs) { fd: NativeFileFd, iovecs: CArrayPointer<iovec>, size: Int ->
        pwritev(fd.fd, iovecs, size, position)
    }.mapLeft { errNo -> errNo.writevErrnoToWriteError(nativeFd, cIovecs) }
}

private fun Int.writevErrnoToWriteError(
    fd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
): WriteError = when (this) {
    EAGAIN -> Again("Blocking write on non-blocking descriptor")
    EBADF -> BadFileDescriptor("Cannot write to $fd: bad file descriptor")
    EDQUOT -> DiskQuota("Cannot write to $fd: disk quota has been exhausted")
    EFBIG -> FileTooBig("Cannot write to $fd: file exceeds maximum size")
    EINTR -> Interrupted("Write operation interrupted on $fd")
    EINVAL -> InvalidArgument("Invalid argument in request `$fd, $iovecs`")
    EIO -> IoError("I/o error on write to $fd")
    ENOSPC -> NoSpace("Cannot write to $fd: no space")
    EPERM -> PermissionDenied("Cannot write to $fd: operation was prevented by a file seal")
    EPIPE -> Pipe("Cannot write to $fd: remove socked closed")
    else -> InvalidArgument("Write error. Errno: `$this`")
}

private fun SeekError.toWriteError(): WriteError = when (this) {
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is Nxio -> this
    is Pipe -> this
    is Overflow -> IoError(this.message)
}
