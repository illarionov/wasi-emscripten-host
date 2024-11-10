/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.ext.toFdAttributesError
import at.released.weh.filesystem.posix.op.open.posixMaskToFsFdFlags
import platform.posix.EBADF
import platform.posix.EINTR
import platform.posix.F_GETFL
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.strerror

internal fun appleFdAttributes(
    channel: NativeFileChannel,
): Either<FdAttributesError, FdAttributesResult> = appleFdAttributes(channel.fd.fd, channel.rights)

internal fun appleFdAttributes(
    fd: NativeDirectoryFd,
    rights: FdRightsBlock,
): Either<FdAttributesError, FdAttributesResult> = appleFdAttributes(fd.posixFd, rights)

private fun appleFdAttributes(
    fd: Int,
    rights: FdRightsBlock,
): Either<FdAttributesError, FdAttributesResult> = either {
    @OpenFileFlagsType
    val fileStatus: Fdflags = readRawFileStatus(fd).bind()
    val fileType = readFileType(fd).bind()
    return FdAttributesResult(
        type = fileType,
        flags = fileStatus,
        rights = rights.rights,
        inheritingRights = rights.rightsInheriting,
    ).right()
}

private fun readRawFileStatus(
    fd: Int,
): Either<FdAttributesError, Fdflags> {
    val exitCode = fcntl(fd, F_GETFL)
    return if (exitCode >= 0) {
        posixMaskToFsFdFlags(exitCode).right()
    } else {
        errno.getflErrnoToFdAttributesError().left()
    }
}

private fun readFileType(
    fd: Int,
): Either<FdAttributesError, Filetype> = appleStatFd(fd)
    .map(StructStat::type)
    .mapLeft(StatError::toFdAttributesError)

private fun Int.getflErrnoToFdAttributesError(): FdAttributesError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINTR -> Interrupted("Interrupted by signal")
    else -> InvalidArgument("Can not get file status. Errno: $this `${strerror(this)}`")
}
