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
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CLOEXEC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOATIME
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.open.fdFdFlagsToPosixMask
import at.released.weh.filesystem.posix.op.open.getFileOpenModeConsideringOpenFlags
import at.released.weh.filesystem.posix.op.open.openFileFlagsToPosixMask
import at.released.weh.host.platform.linux.RESOLVE_BENEATH
import at.released.weh.host.platform.linux.RESOLVE_CACHED
import at.released.weh.host.platform.linux.RESOLVE_IN_ROOT
import at.released.weh.host.platform.linux.RESOLVE_NO_MAGICLINKS
import at.released.weh.host.platform.linux.RESOLVE_NO_SYMLINKS
import at.released.weh.host.platform.linux.RESOLVE_NO_XDEV
import at.released.weh.host.platform.linux.SYS_openat2
import at.released.weh.host.platform.linux.open_how
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.E2BIG
import platform.posix.EAGAIN
import platform.posix.EEXIST
import platform.posix.EINVAL
import platform.posix.ELOOP
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.EXDEV
import platform.posix.errno
import platform.posix.memset
import platform.posix.syscall

internal fun linuxOpenFileOrDirectory(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
    @FileMode mode: Int?,
    resolveFlags: Set<ResolveModeFlag> = setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS),
): Either<OpenError, FileDirectoryFd> = either<OpenError, FileDirectoryFd> {
    val isInAppendMode = fdFlags and FD_APPEND == FD_APPEND
    val fdFlagsNoAppend = fdFlags and FD_APPEND.inv()

    val existingFileType: Filetype? = getFileType(baseDirectoryFd, path, flags).bind()

    if (existingFileType == DIRECTORY) {
        val directoryFlags = flags and (O_NOFOLLOW or O_NOATIME or O_CLOEXEC) or O_DIRECTORY
        return linuxOpenRaw(
            baseDirectoryFd = baseDirectoryFd,
            path = path,
            flags = directoryFlags,
            fdFlags = fdFlagsNoAppend,
            mode = null,
            resolveFlags = resolveFlags,
        ).map {
            FileDirectoryFd.Directory(NativeDirectoryFd(it))
        }
    }

    if (flags and O_DIRECTORY == O_DIRECTORY) {
        if (existingFileType != null) {
            raise(NotDirectory("Not a directory"))
        } else {
            raise(NoEntry("Path not exists"))
        }
    }

    linuxOpenRaw(
        baseDirectoryFd = baseDirectoryFd,
        path = path,
        flags = flags,
        fdFlags = fdFlagsNoAppend,
        mode = getFileOpenModeConsideringOpenFlags(flags, mode),
        resolveFlags = resolveFlags,
    ).map {
        FileDirectoryFd.File(it, isInAppendMode)
    }.bind()
}

internal fun linuxOpenRaw(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
    @FileMode mode: Int?,
    resolveFlags: Set<ResolveModeFlag> = setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS),
): Either<OpenError, Int> {
    val errorOrFd = memScoped {
        val openHow: open_how = alloc<open_how> {
            memset(ptr, 0, sizeOf<open_how>().toULong())
            this.flags = getLinuxOpenFileFlags(flags, fdFlags)
            this.mode = mode?.toULong() ?: 0UL
            this.resolve = resolveFlags.toResolveMask()
        }
        syscall(
            __sysno = SYS_openat2.toLong(),
            baseDirectoryFd.linuxFd,
            path.kString.cstr,
            openHow.ptr,
            sizeOf<open_how>().toULong(),
        )
    }
    return if (errorOrFd < 0) {
        errno.openat2ErrNoToOpenError().left()
    } else {
        errorOrFd.toInt().right()
    }
}

private fun getFileType(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
): Either<OpenError, Filetype?> {
    return linuxStat(baseDirectoryFd, path, flags and O_NOFOLLOW != O_NOFOLLOW).fold(
        ifRight = {
            it.type.right()
        },
        ifLeft = {
            if (it is NoEntry) {
                null.right()
            } else {
                it.toOpenError().left()
            }
        },
    )
}

private fun getLinuxOpenFileFlags(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): ULong {
    return openFileFlagsToPosixMask(flags) or fdFdFlagsToPosixMask(fdFlags)
}

private fun Int.openat2ErrNoToOpenError(): OpenError = when (this) {
    E2BIG -> InvalidArgument("E2BIG: extension is not supported")
    EAGAIN -> Again("Operation cannot be performed")
    EEXIST -> Exists("File exists")
    EINVAL -> InvalidArgument("Invalid argument")
    ELOOP -> TooManySymbolicLinks("Too many symbolic or magic links")
    EXDEV -> NotCapable("Escape from the root detected")
    ENOENT -> NoEntry("No such file or directory")
    ENOTDIR -> NotDirectory("Path is not a directory")
    else -> InvalidArgument("Unknown errno $this")
}

private fun StatError.toOpenError(): OpenError = this as OpenError

internal sealed class FileDirectoryFd {
    class File(
        val fd: NativeFileFd,
        val isInAppendMode: Boolean,
    ) : FileDirectoryFd() {
        constructor(fd: Int, isInAppendMode: Boolean) : this(NativeFileFd(fd), isInAppendMode)
    }

    class Directory(
        val fd: NativeDirectoryFd,
    ) : FileDirectoryFd()
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
