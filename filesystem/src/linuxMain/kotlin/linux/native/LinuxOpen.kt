/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ACCMODE
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.platform.linux.RESOLVE_BENEATH
import at.released.weh.filesystem.platform.linux.RESOLVE_CACHED
import at.released.weh.filesystem.platform.linux.RESOLVE_IN_ROOT
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_MAGICLINKS
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_SYMLINKS
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_XDEV
import at.released.weh.filesystem.platform.linux.SYS_openat2
import at.released.weh.filesystem.platform.linux.open_how
import at.released.weh.filesystem.posix.NativeFd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.E2BIG
import platform.posix.EAGAIN
import platform.posix.EINVAL
import platform.posix.ELOOP
import platform.posix.EXDEV
import platform.posix.errno
import platform.posix.memset
import platform.posix.syscall

internal fun linuxOpen(
    baseDirectoryFd: NativeFd,
    path: String,
    @OpenFileFlags flags: Int,
    @FileMode mode: Int,
): Either<OpenError, NativeFd> {
    val errorOrFd = memScoped {
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().toULong())
            this.flags = openFileFlagsToLinuxMask(flags)
            this.mode = mode.toULong()
            this.resolve = setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS).toResolveMask()
        }
        syscall(
            __sysno = SYS_openat2.toLong(),
            baseDirectoryFd.fd,
            path.cstr,
            openHow.ptr,
            sizeOf<open_how>().toULong(),
        )
    }
    return if (errorOrFd < 0) {
        errno.errNoToOpenError().left()
    } else {
        val nativeFd = NativeFd(errorOrFd.toInt())
        nativeFd.right()
    }
}

private val openFileFlagsMaskToPosixMask = listOf(
    OpenFileFlag.O_CREAT to platform.posix.O_CREAT,
    OpenFileFlag.O_EXCL to platform.posix.O_EXCL,
    OpenFileFlag.O_NOCTTY to platform.posix.O_NOCTTY,
    OpenFileFlag.O_TRUNC to platform.posix.O_TRUNC,
    OpenFileFlag.O_APPEND to platform.posix.O_APPEND,
    OpenFileFlag.O_NONBLOCK to platform.posix.O_NONBLOCK,
    OpenFileFlag.O_NDELAY to platform.posix.O_NDELAY,
    OpenFileFlag.O_DSYNC to platform.posix.O_DSYNC,
    OpenFileFlag.O_ASYNC to platform.posix.O_ASYNC,
    OpenFileFlag.O_DIRECTORY to platform.posix.O_DIRECTORY,
    OpenFileFlag.O_NOFOLLOW to platform.posix.O_NOFOLLOW,
    OpenFileFlag.O_CLOEXEC to platform.posix.O_CLOEXEC,
    OpenFileFlag.O_SYNC to platform.posix.O_SYNC,
)

@Suppress("CyclomaticComplexMethod")
internal fun openFileFlagsToLinuxMask(
    @OpenFileFlags openFlags: Int,
): ULong {
    var mask = when (val mode = (openFlags and O_ACCMODE)) {
        OpenFileFlag.O_RDONLY -> platform.posix.O_RDONLY
        OpenFileFlag.O_WRONLY -> platform.posix.O_WRONLY
        OpenFileFlag.O_RDWR -> platform.posix.O_RDWR
        else -> error("Unknown mode $mode")
    }

    openFileFlagsMaskToPosixMask.forEach { (testMask, posixMask) ->
        if (openFlags and testMask == testMask) {
            mask = mask or posixMask
        }
    }
    // O_DIRECT, O_LARGEFILE, O_NOATIME: Not supported
    // O_PATH, O_TMPFILE: not supported, should be added?

    return mask.toULong()
}

private fun Int.errNoToOpenError(): OpenError = when (this) {
    E2BIG -> InvalidArgument("E2BIG: extension is not supported")
    EAGAIN -> Again("operation cannot be performed")
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symbolic or magic links")
    EXDEV -> NotCapable("Escape from the root detected")
    else -> InvalidArgument("Unknown errno $this")
}

internal enum class ResolveModeFlag {
    RESOLVE_BENEATH,
    RESOLVE_IN_ROOT,
    RESOLVE_NO_MAGICLINKS,
    RESOLVE_NO_SYMLINKS,
    RESOLVE_NO_XDEV,
    RESOLVE_CACHED,
}

private fun Set<ResolveModeFlag>.toResolveMask(): ULong {
    var mask = 0UL
    if (contains(ResolveModeFlag.RESOLVE_BENEATH)) {
        mask = mask and RESOLVE_BENEATH.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_IN_ROOT)) {
        mask = mask and RESOLVE_IN_ROOT.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_MAGICLINKS)) {
        mask = mask and RESOLVE_NO_MAGICLINKS.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_SYMLINKS)) {
        mask = mask and RESOLVE_NO_SYMLINKS.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_XDEV)) {
        mask = mask and RESOLVE_NO_XDEV.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_CACHED)) {
        mask = mask and RESOLVE_CACHED.toULong()
    }
    return mask
}
