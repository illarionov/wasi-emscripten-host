/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FileTooBig
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoBufferSpace
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENODEV
import platform.posix.ENOSPC
import platform.posix.ENOTSUP
import platform.posix.ESPIPE
import platform.posix.F_ALLOCATEALL
import platform.posix.F_ALLOCATEPERSIST
import platform.posix.F_PEOFPOSMODE
import platform.posix.F_PREALLOCATE
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.fstore
import platform.posix.strerror

internal fun appleFallocate(
    fd: NativeFileFd,
    offset: Long,
    length: Long,
): Either<FallocateError, Unit> = either {
    if (offset < 0) {
        raise(InvalidArgument("Incorrect offset $offset"))
    }
    if (length <= 0) {
        raise(InvalidArgument("Incorrect length $length"))
    }

    val fileSize = appleStatFd(fd.fd).map(StructStat::size).mapLeft(StatError::toFallocateError).bind()

    val appendBytes: Long = (offset + length - fileSize).coerceAtLeast(0)
    if (appendBytes > 0) {
        val preallocatedOrErrno = preallocate(fd.fd, appendBytes)
        when {
            preallocatedOrErrno < 0L -> raise(fPreallocateCodeToFallocateError(-preallocatedOrErrno.toInt()))
            preallocatedOrErrno != appendBytes -> raise(
                IoError("Preallocated $preallocatedOrErrno bytes while requested $appendBytes "),
            )
        }
        // write last byte to change file size
        appleWrite(
            fd,
            listOf(FileSystemByteBuffer(ByteArray(1))),
            ReadWriteStrategy.Position(appendBytes + fileSize - 1),
        ).mapLeft { it.toFallocateError() }.bind()
    }
}

private fun preallocate(fd: Int, appendBytes: Long): Long = memScoped {
    val fstore: fstore = alloc<fstore>().apply {
        fst_flags = (F_ALLOCATEALL or F_ALLOCATEPERSIST).toUInt()
        fst_posmode = F_PEOFPOSMODE
        fst_offset = 0
        fst_length = appendBytes
    }
    if (fcntl(fd, F_PREALLOCATE, fstore.ptr) == -1) {
        -errno.toLong()
    } else {
        fstore.fst_length
    }
}

private fun fPreallocateCodeToFallocateError(code: Int): FallocateError = when (code) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EFBIG -> FileTooBig("File size too big")
    EINVAL -> InvalidArgument("Incorrect offset or length")
    EINTR -> Interrupted("Fallocate interrupted by signal")
    ENODEV -> BadFileDescriptor("Not a file")
    ENOSPC -> NoSpace("Can not pre-allocate: no space")
    ENOTSUP -> NotSupported("Fallocate is not not supported by file system")
    ESPIPE -> BadFileDescriptor("Can not fallocate on a pipe")
    else -> InvalidArgument("Other error $code (${strerror(code)?.toKStringFromUtf8()})")
}

private fun StatError.toFallocateError(): FallocateError = when (this) {
    is AccessDenied -> IoError(this.message)
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    else -> InvalidArgument(this.message)
}

@Suppress("CyclomaticComplexMethod")
private fun WriteError.toFallocateError(): FallocateError = when (this) {
    is Again -> IoError("Write last block failed: ${this.message}")
    is BadFileDescriptor -> this
    is DiskQuota -> NoSpace(this.message)
    is FileTooBig -> this
    is Interrupted -> this
    is InvalidArgument -> this
    is IoError -> this
    is NoBufferSpace -> NoSpace(this.message)
    is NoSpace -> this
    is NotCapable -> IoError(this.message)
    is Nxio -> IoError(this.message)
    is PermissionDenied -> IoError(this.message)
    is Pipe -> IoError(this.message)
    is ReadOnlyFileSystem -> IoError(this.message)
}
