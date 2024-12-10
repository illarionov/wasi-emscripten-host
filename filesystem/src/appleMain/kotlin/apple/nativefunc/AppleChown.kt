/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.ext.followSymlinksAsAtSymlinkFlags
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.chown.PosixChownErrorMapper
import platform.posix.errno
import platform.posix.fchown
import platform.posix.fchownat

internal fun appleChown(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    owner: Int,
    group: Int,
    followSymlinks: Boolean,
): Either<ChownError, Unit> {
    val resultCode = fchownat(
        baseDirectoryFd.posixFd,
        path.kString,
        owner.toUInt(),
        group.toUInt(),
        followSymlinksAsAtSymlinkFlags(followSymlinks),
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        PosixChownErrorMapper.errnoToChownError(errno).left()
    }
}

internal fun appleChownFd(
    fd: NativeDirectoryFd,
    owner: Int,
    group: Int,
): Either<ChownError, Unit> {
    require(fd != NativeDirectoryFd.CURRENT_WORKING_DIRECTORY)
    return appleChownFd(NativeFileFd(fd.raw), owner, group)
}

internal fun appleChownFd(
    nativeFd: NativeFileFd,
    owner: Int,
    group: Int,
): Either<ChownError, Unit> {
    val resultCode = fchown(nativeFd.fd, owner.toUInt(), group.toUInt())
    return if (resultCode == 0) {
        Unit.right()
    } else {
        PosixChownErrorMapper.errnoToChownFdError(errno).left()
    }
}
