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
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import at.released.weh.filesystem.posix.NativeFd
import kotlinx.cinterop.CArrayPointer
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.ENOSPC
import platform.posix.ENXIO
import platform.posix.EPERM
import platform.posix.EPIPE
import platform.posix.SEEK_CUR
import platform.posix.errno
import platform.posix.iovec
import platform.posix.lseek
import platform.posix.pwritev
import platform.posix.writev

internal fun posixWrite(
    nativeFd: NativeFd,
    cIovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
) = when (strategy) {
    CHANGE_POSITION -> posixWriteChangePosition(nativeFd, cIovecs)
    DO_NOT_CHANGE_POSITION -> posixWriteDoNotChangePosition(nativeFd, cIovecs)
}

private fun posixWriteChangePosition(
    nativeFd: NativeFd,
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> =
    callReadWrite(nativeFd, cIovecs) { fd: NativeFd, iovecs: CArrayPointer<iovec>, size: Int ->
        writev(fd.fd, iovecs, size)
    }.mapLeft { errNo -> errNo.errnoToWriteError(nativeFd, cIovecs) }

private fun posixWriteDoNotChangePosition(
    nativeFd: NativeFd,
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> {
    val currentPosition = lseek(nativeFd.fd, 0, SEEK_CUR)
    return if (currentPosition < 0) {
        errno.errnoSeekToWriteError(nativeFd).left()
    } else {
        callReadWrite(nativeFd, cIovecs) { fd: NativeFd, iovecs: CArrayPointer<iovec>, size: Int ->
            pwritev(fd.fd, iovecs, size, currentPosition)
        }.mapLeft { errNo -> errNo.errnoToWriteError(nativeFd, cIovecs) }
    }
}

private fun Int.errnoToWriteError(
    fd: NativeFd,
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

private fun Int.errnoSeekToWriteError(fd: NativeFd): WriteError = when (this) {
    EBADF -> BadFileDescriptor("Cannot seek on $fd: bad file descriptor")
    EINVAL -> InvalidArgument("seek() failed. Invalid argument. Fd: $fd")
    ENXIO -> Nxio("Trying to seek beyond end of file. Fd: $fd")
    else -> InvalidArgument("Seel failed: unexpected error. Errno: `$this`")
}
