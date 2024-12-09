/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.platform.linux.AT_EACCESS
import at.released.weh.filesystem.platform.linux.AT_SYMLINK_NOFOLLOW
import at.released.weh.filesystem.platform.linux.SYS_faccessat2
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.nativefunc.CheckAccessMapper.checkAccessErrnoToCheckAccessError
import at.released.weh.filesystem.posix.nativefunc.CheckAccessMapper.fileAccessibilityCheckToPosixModeFlags
import platform.posix.errno
import platform.posix.syscall

internal fun linuxCheckAccess(
    baseDirectoryFd: NativeDirectoryFd,
    path: RealPath,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean = true,
    followSymlinks: Boolean = false,
): Either<CheckAccessError, Unit> =
    linuxCheckAccess(baseDirectoryFd.linuxFd, path, mode, useEffectiveUserId, followSymlinks)

private fun linuxCheckAccess(
    nativeFdOrArCwd: Int,
    path: RealPath,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean,
    followSymlinks: Boolean,
): Either<CheckAccessError, Unit> {
    val resultCode = syscall(
        SYS_faccessat2.toLong(),
        nativeFdOrArCwd,
        path,
        fileAccessibilityCheckToPosixModeFlags(mode),
        getCheckAccessFlags(useEffectiveUserId, followSymlinks),
    )
    return if (resultCode == 0L) {
        Unit.right()
    } else {
        checkAccessErrnoToCheckAccessError(errno).left()
    }
}

private fun getCheckAccessFlags(
    useEffectiveUserId: Boolean,
    followSymlinks: Boolean,
): Int {
    var mask = 0
    if (useEffectiveUserId) {
        mask = mask and AT_EACCESS
    }
    if (!followSymlinks) {
        mask = mask and AT_SYMLINK_NOFOLLOW
    }
    return mask
}
