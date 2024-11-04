/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.platform.linux.symlinkat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.preopened.VirtualPath
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EACCES
import platform.posix.EBADF
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EIO
import platform.posix.ELOOP
import platform.posix.ENAMETOOLONG
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.EPERM
import platform.posix.EROFS
import platform.posix.errno
import platform.posix.strerror

internal fun linuxSymlink(
    target: VirtualPath,
    linkPath: VirtualPath,
    linkPathBaseDirectoryFd: NativeDirectoryFd,
): Either<SymlinkError, Unit> {
    val resultCode = symlinkat(
        target,
        linkPathBaseDirectoryFd.linuxFd,
        linkPath,
    )
    return if (resultCode == 0) {
        Unit.right()
    } else {
        errno.errnoToSymlinkError().left()
    }
}

private fun Int.errnoToSymlinkError(): SymlinkError = when (this) {
    EACCES -> AccessDenied("Access to linkpath denied")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDQUOT -> DiskQuota("User quota on filesystem resources has been exhausted")
    EEXIST -> Exists("Linkpath exists")
    EIO -> IoError("I/O error")
    ELOOP -> TooManySymbolicLinks("Too many symbolic links")
    ENAMETOOLONG -> NameTooLong("Linkpath name too long")
    ENOSPC -> NoSpace("Can not symlink: no enough space")
    ENOTDIR -> NotDirectory("Error while resolving request path: not a directory")
    EPERM -> AccessDenied("No permission to create symlink.")
    EROFS -> ReadOnlyFileSystem(
        "Write permission requested for a file on a read-only filesystem.",
    )

    else -> IoError("Other error: $this (${strerror(this)?.toKStringFromUtf8()})")
}
