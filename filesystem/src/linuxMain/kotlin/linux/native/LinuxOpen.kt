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
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.fdFdFlagsToLinuxMask
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.ext.openFileFlagsToLinuxMask
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CREAT
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_TMPFILE
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.platform.linux.RESOLVE_BENEATH
import at.released.weh.filesystem.platform.linux.RESOLVE_CACHED
import at.released.weh.filesystem.platform.linux.RESOLVE_IN_ROOT
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_MAGICLINKS
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_SYMLINKS
import at.released.weh.filesystem.platform.linux.RESOLVE_NO_XDEV
import at.released.weh.filesystem.platform.linux.SYS_openat2
import at.released.weh.filesystem.platform.linux.open_how
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.E2BIG
import platform.posix.EAGAIN
import platform.posix.EINVAL
import platform.posix.ELOOP
import platform.posix.ENOENT
import platform.posix.EXDEV
import platform.posix.errno
import platform.posix.memset
import platform.posix.syscall

internal fun linuxOpen(
    baseDirectoryFd: NativeDirectoryFd,
    path: String,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
    @FileMode mode: Int?,
    resolveFlags: Set<ResolveModeFlag> = setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS),
): Either<OpenError, Int> {
    @Suppress("MagicNumber")
    val linuxOpenMode = when {
        flags and (O_CREAT or O_TMPFILE) == 0 -> 0
        mode != null -> mode
        flags and O_DIRECTORY == O_DIRECTORY -> 0b111_101_101
        else -> 0b110_100_000
    }

    val errorOrFd = memScoped {
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().toULong())
            this.flags = getLinuxOpenFileFlags(flags, fdFlags)
            this.mode = linuxOpenMode.toULong()
            this.resolve = resolveFlags.toResolveMask()
        }
        syscall(
            __sysno = SYS_openat2.toLong(),
            baseDirectoryFd.linuxFd,
            path.cstr,
            openHow.ptr,
            sizeOf<open_how>().toULong(),
        )
    }
    return if (errorOrFd < 0) {
        errno.errNoToOpenError().left()
    } else {
        errorOrFd.toInt().right()
    }
}

private fun getLinuxOpenFileFlags(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): ULong {
    return openFileFlagsToLinuxMask(flags) or fdFdFlagsToLinuxMask(fdFlags)
}

private fun Int.errNoToOpenError(): OpenError = when (this) {
    E2BIG -> InvalidArgument("E2BIG: extension is not supported")
    EAGAIN -> Again("Operation cannot be performed")
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symbolic or magic links")
    EXDEV -> NotCapable("Escape from the root detected")
    ENOENT -> NoEntry("No such file or directory")
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
        mask = mask or RESOLVE_BENEATH.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_IN_ROOT)) {
        mask = mask or RESOLVE_IN_ROOT.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_MAGICLINKS)) {
        mask = mask or RESOLVE_NO_MAGICLINKS.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_SYMLINKS)) {
        mask = mask or RESOLVE_NO_SYMLINKS.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_NO_XDEV)) {
        mask = mask or RESOLVE_NO_XDEV.toULong()
    }
    if (contains(ResolveModeFlag.RESOLVE_CACHED)) {
        mask = mask or RESOLVE_CACHED.toULong()
    }
    return mask
}
