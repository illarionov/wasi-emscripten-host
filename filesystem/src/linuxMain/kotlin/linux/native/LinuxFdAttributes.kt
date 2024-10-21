/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.STDIO_FD_RIGHTS
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.ext.linuxMaskToFsFdFlags
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.model.Filetype.BLOCK_DEVICE
import at.released.weh.filesystem.model.Filetype.CHARACTER_DEVICE
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.model.Filetype.SOCKET_DGRAM
import at.released.weh.filesystem.model.Filetype.SOCKET_STREAM
import at.released.weh.filesystem.model.Filetype.SYMBOLIC_LINK
import at.released.weh.filesystem.model.Filetype.UNKNOWN
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.DIRECTORY_BASE_RIGHTS
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FILE_BASE_RIGHTS
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.PATH_SYMLINK
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.SOCK_ACCEPT
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.SOCK_SHUTDOWN
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.F_GETFL
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.strerror

internal fun linuxFdAttributes(
    fd: NativeFileFd,
): Either<FdAttributesError, FdAttributesResult> = linuxFdAttributes(fd.fd)

internal fun linuxFdAttributes(
    fd: NativeDirectoryFd,
): Either<FdAttributesError, FdAttributesResult> = linuxFdAttributes(fd.linuxFd)

private fun linuxFdAttributes(
    fd: Int,
): Either<FdAttributesError, FdAttributesResult> = either {
    @OpenFileFlagsType
    val fileStatus: Fdflags = readFileStatus(fd).bind()
    val fileType = readFileType(fd).bind()

    // XXX: check
    val rights = getFdRightsByFileType(fileType)

    val inheritingRights = when (fileType) {
        UNKNOWN -> 0
        BLOCK_DEVICE -> 0
        CHARACTER_DEVICE -> 0
        DIRECTORY -> rights or FILE_BASE_RIGHTS
        REGULAR_FILE -> rights
        SOCKET_DGRAM -> rights
        SOCKET_STREAM -> rights
        SYMBOLIC_LINK -> rights
    }

    return FdAttributesResult(
        type = fileType,
        flags = fileStatus,
        rights = rights,
        inheritingRights = inheritingRights,
    ).right()
}

private fun readFileStatus(
    fd: Int,
): Either<FdAttributesError, Fdflags> {
    val exitCode = fcntl(fd, F_GETFL)
    return if (exitCode >= 0) {
        linuxMaskToFsFdFlags(exitCode).right()
    } else {
        errno.getflErrnoToFdAttributesError().left()
    }
}

private fun readFileType(
    fd: Int,
): Either<FdAttributesError, Filetype> = linuxStatFd(fd)
    .map(StructStat::type)
    .mapLeft(StatError::toFdAttributesError)

private fun getFdRightsByFileType(fileType: Filetype) = when (fileType) {
    UNKNOWN -> 0
    BLOCK_DEVICE -> 0
    CHARACTER_DEVICE -> STDIO_FD_RIGHTS
    DIRECTORY -> DIRECTORY_BASE_RIGHTS or FILE_BASE_RIGHTS
    REGULAR_FILE -> FILE_BASE_RIGHTS
    SYMBOLIC_LINK -> FILE_BASE_RIGHTS or PATH_SYMLINK
    SOCKET_DGRAM -> FILE_BASE_RIGHTS or SOCK_SHUTDOWN
    SOCKET_STREAM -> FILE_BASE_RIGHTS or SOCK_SHUTDOWN or SOCK_ACCEPT
}

private fun Int.getflErrnoToFdAttributesError(): FdAttributesError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINTR -> Interrupted("Interrupted by signal")
    else -> InvalidArgument("Can not get file status. Errno: $this `${strerror(this)}`")
}

private fun StatError.toFdAttributesError(): FdAttributesError = when (this) {
    is AccessDenied -> this
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    is NameTooLong -> InvalidArgument(this.message)
    is NoEntry -> InvalidArgument(this.message)
    is NotCapable -> InvalidArgument(this.message)
    is NotDirectory -> InvalidArgument(this.message)
    is TooManySymbolicLinks -> this
}
