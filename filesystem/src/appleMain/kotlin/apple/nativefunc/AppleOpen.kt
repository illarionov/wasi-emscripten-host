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
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CLOEXEC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CREAT
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOATIME
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.platform.apple.openat
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.op.open.fdFdFlagsToPosixMask
import at.released.weh.filesystem.posix.op.open.getFileOpenModeConsideringOpenFlags
import at.released.weh.filesystem.posix.op.open.openFileFlagsToPosixMask
import platform.posix.EACCES
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EDEADLK
import platform.posix.EDQUOT
import platform.posix.EEXIST
import platform.posix.EILSEQ
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.EIO
import platform.posix.EISDIR
import platform.posix.ELOOP
import platform.posix.EMFILE
import platform.posix.ENAMETOOLONG
import platform.posix.ENFILE
import platform.posix.ENOENT
import platform.posix.ENOSPC
import platform.posix.ENOTDIR
import platform.posix.ENXIO
import platform.posix.EOPNOTSUPP
import platform.posix.EOVERFLOW
import platform.posix.EROFS
import platform.posix.ETXTBSY
import platform.posix.EWOULDBLOCK
import platform.posix.errno

internal fun appleOpenFileOrDirectory(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
    @FileMode mode: Int?,
): Either<OpenError, FileDirectoryFd> = either<OpenError, FileDirectoryFd> {
    val isInAppendMode = fdFlags and FD_APPEND == FD_APPEND

    val existingFileType: Filetype? = getFileType(baseDirectoryFd, path, flags).bind()

    if (existingFileType == DIRECTORY) {
        val directoryFlags = flags and (O_NOFOLLOW or O_NOATIME or O_CLOEXEC) or O_DIRECTORY
        return appleOpenRaw(
            baseDirectoryFd = baseDirectoryFd,
            path = path,
            flags = directoryFlags,
            fdFlags = fdFlags,
            mode = null,
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

    appleOpenRaw(
        baseDirectoryFd = baseDirectoryFd,
        path = path,
        flags = flags,
        fdFlags = fdFlags,
        mode = getFileOpenModeConsideringOpenFlags(flags, mode),
    ).map {
        FileDirectoryFd.File(it, isInAppendMode)
    }.bind()
}

internal fun appleOpenRaw(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
    @FileMode mode: Int?,
): Either<OpenError, Int> {
    val openFlags = getAppleOpenFileFlags(flags, fdFlags).toInt()
    val errorOrFd = if (flags and O_CREAT == O_CREAT) {
        val realMode = mode?.toULong() ?: 0UL
        openat(baseDirectoryFd.posixFd, path.kString, openFlags, realMode)
    } else {
        openat(baseDirectoryFd.posixFd, path.kString, openFlags)
    }
    return if (errorOrFd < 0) {
        errno.openat2ErrNoToOpenErrorApple().left()
    } else {
        errorOrFd.right()
    }
}

private fun getFileType(
    baseDirectoryFd: NativeDirectoryFd,
    path: PosixRealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
): Either<OpenError, Filetype?> {
    return appleStat(baseDirectoryFd, path, flags and O_NOFOLLOW != O_NOFOLLOW).fold(
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

private fun getAppleOpenFileFlags(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): ULong {
    return openFileFlagsToPosixMask(flags) or fdFdFlagsToPosixMask(fdFlags)
}

@Suppress("CyclomaticComplexMethod", "DUPLICATE_LABEL_IN_WHEN")
private fun Int.openat2ErrNoToOpenErrorApple(): OpenError = when (this) {
    EACCES -> AccessDenied("No permission")
    EAGAIN -> Again("Operation cannot be performed")
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EDEADLK -> AccessDenied("dataless directory materialization is not allowed")
    EDQUOT -> DiskQuota("User's quota of disk block's has been exhausted")
    EEXIST -> Exists("File exists")
    EILSEQ -> NotCapable("Filename does not match encoding rules")
    EINTR -> Interrupted("Operation interrupted by signal")
    EINVAL -> InvalidArgument("Invalid argument")
    EIO -> IoError("I/o error")
    EISDIR -> PathIsDirectory("Directory can not be opened for writing or executing")
    ELOOP -> TooManySymbolicLinks("Too many symbolic links")
    EMFILE -> Mfile("Limit on open file descriptors has been exhausted")
    ENAMETOOLONG -> NameTooLong("Name too long")
    ENFILE -> Nfile("System file table is full")
    ENOENT -> NoEntry("No such file or directory")
    ENOSPC -> NoSpace("No space left on device")
    ENOTDIR -> NotDirectory("Target is not a directory")
    ENXIO -> Nxio("Devie of the special file is not available")
    EOPNOTSUPP -> InvalidArgument("Filesystem does not support locking")
    EOVERFLOW -> IoError("Size of the file does not fit in off_t")
    EROFS -> ReadOnlyFileSystem("Read-only file system")
    ETXTBSY -> TextFileBusy("Can not write to the executed file")
    EWOULDBLOCK -> IoError("File can not be locked without blocking")
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
