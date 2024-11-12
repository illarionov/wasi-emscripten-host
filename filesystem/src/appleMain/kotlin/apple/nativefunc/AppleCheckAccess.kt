/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.nativefunc.CheckAccessMapper.checkAccessErrnoToCheckAccessError
import at.released.weh.filesystem.posix.nativefunc.CheckAccessMapper.fileAccessibilityCheckToPosixModeFlags
import platform.posix.AT_EACCESS
import platform.posix.AT_SYMLINK_NOFOLLOW
import platform.posix.errno
import platform.posix.faccessat

internal fun appleCheckAccess(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean = false,
    followSymlinks: Boolean = false,
): Either<CheckAccessError, Unit> =
    appleCheckAccess(baseDirectoryFd.posixFd, path, mode, useEffectiveUserId, followSymlinks)

private fun appleCheckAccess(
    nativeFdOrArCwd: Int,
    path: String,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean,
    followSymlinks: Boolean,
): Either<CheckAccessError, Unit> {
    val resultCode = faccessat(
        nativeFdOrArCwd,
        path,
        fileAccessibilityCheckToPosixModeFlags(mode),
        getCheckAccessFlags(useEffectiveUserId, followSymlinks),
    )
    return if (resultCode == 0) {
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
