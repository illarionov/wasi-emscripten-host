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
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.ext.linuxMaskToFsFdFlags
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.F_GETFL
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.strerror

internal fun linuxFdAttributes(
    channel: NativeFileChannel,
): Either<FdAttributesError, FdAttributesResult> =
    linuxFdAttributes(channel.fd.fd, channel.isInAppendMode, channel.rights)

internal fun linuxFdAttributes(
    fd: NativeDirectoryFd,
    rights: FdRightsBlock,
): Either<FdAttributesError, FdAttributesResult> = linuxFdAttributes(fd.linuxFd, false, rights)

private fun linuxFdAttributes(
    fd: Int,
    isInAppendMode: Boolean,
    rights: FdRightsBlock,
): Either<FdAttributesError, FdAttributesResult> = either {
    @OpenFileFlagsType
    val fileStatus: Fdflags = readFileStatus(fd, isInAppendMode).bind()
    val fileType = readFileType(fd).bind()
    return FdAttributesResult(
        type = fileType,
        flags = fileStatus,
        rights = rights.rights,
        inheritingRights = rights.rightsInheriting,
    ).right()
}

private fun readFileStatus(
    fd: Int,
    isInAppendMode: Boolean,
): Either<FdAttributesError, Fdflags> = readRawFileStatus(fd).map { fdFlags ->
    if (isInAppendMode) {
        fdFlags or FdFlag.FD_APPEND
    } else {
        fdFlags
    }
}

private fun readRawFileStatus(
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
