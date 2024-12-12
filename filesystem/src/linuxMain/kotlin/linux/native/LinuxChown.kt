/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.platform.linux.fchownat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.chown.PosixChownErrorMapper
import platform.posix.errno
import platform.posix.fchown

internal fun linuxChown(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    owner: Int,
    group: Int,
    followSymlinks: Boolean,
): Either<ChownError, Unit> {
    val resultCode = fchownat(
        baseDirectoryFd.linuxFd,
        path.kString,
        owner,
        group,
        getChownFlags(followSymlinks),
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        PosixChownErrorMapper.errnoToChownError(errno).left()
    }
}

internal fun linuxChownFd(
    fd: NativeDirectoryFd,
    owner: Int,
    group: Int,
): Either<ChownError, Unit> {
    require(fd != NativeDirectoryFd.CURRENT_WORKING_DIRECTORY)
    return linuxChownFd(NativeFileFd(fd.raw), owner, group)
}

internal fun linuxChownFd(
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

private fun getChownFlags(followSymlinks: Boolean): Int = if (!followSymlinks) {
    AT_SYMLINK_NOFOLLOW
} else {
    0
}
