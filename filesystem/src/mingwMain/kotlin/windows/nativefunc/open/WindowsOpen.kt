/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.FdFlag.FD_DSYNC
import at.released.weh.filesystem.model.FdFlag.FD_RSYNC
import at.released.weh.filesystem.model.FdFlag.FD_SYNC
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ACCMODE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDONLY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_TMPFILE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_WRONLY
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.posix.ext.validatePath
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtCreateFileEx
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_ATTRIBUTE_TEMPORARY
import platform.windows.FILE_CREATE
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_GENERIC_READ
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_FOR_BACKUP_INTENT
import platform.windows.FILE_OPEN_IF
import platform.windows.FILE_OPEN_REPARSE_POINT
import platform.windows.FILE_OVERWRITE
import platform.windows.FILE_OVERWRITE_IF
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES
import platform.windows.FILE_WRITE_THROUGH
import platform.windows.HANDLE

internal fun windowsOpenFileOrDirectory(
    baseHandle: HANDLE?,
    path: RealPath,
    @OpenFileFlagsType flags: OpenFileFlags,
    @FdflagsType fdFlags: Fdflags,
): Either<OpenError, FileDirectoryHandle> = either<OpenError, FileDirectoryHandle> {
    val isInAppendMode = fdFlags and FD_APPEND == FD_APPEND
    val fdFlagsNoAppend = fdFlags and FD_APPEND.inv()
    val isDirectoryRequest = path.endsWith("/") || path.endsWith("\\")
    val isDirectoryOrPathRequest = flags and OpenFileFlag.O_DIRECTORY == OpenFileFlag.O_DIRECTORY ||
            flags and OpenFileFlag.O_PATH == OpenFileFlag.O_PATH ||
            isDirectoryRequest
    if (isDirectoryOrPathRequest) {
        if (flags and OpenFileFlag.O_CREAT == OpenFileFlag.O_CREAT) {
            return InvalidArgument("O_CREAT cannot be used to create directories").left()
        }
        if (flags and O_ACCMODE != O_RDONLY) {
            return InvalidArgument("Directory should be opened in read/only").left()
        }
    }

    // TODO: validate windows path
    validatePath(path).bind()

    val desiredAccess = getDesiredAccess(flags, isDirectoryOrPathRequest)
    val fileAttributes = getFileAttributes(flags, isDirectoryOrPathRequest)
    val createDisposition = getCreateDisposition(flags, isDirectoryRequest)
    val followSymlinks = flags and O_NOFOLLOW != O_NOFOLLOW
    val createOptions = getCreateOptions(fdFlagsNoAppend, isDirectoryOrPathRequest, followSymlinks)

    return windowsNtCreateFileEx(
        rootHandle = baseHandle,
        path = path,
        desiredAccess = desiredAccess,
        fileAttributes = fileAttributes,
        createDisposition = createDisposition,
        createOptions = createOptions,
    ).flatMap { handle: HANDLE ->
        handle.getFileAttributeTagInfo()
            .mapLeft(StatError::toOpenError)
            .flatMap {
                if (it.fileAttributes.isSymlinkOrReparsePoint) {
                    handle.close().onLeft { /* ignore error */ }
                    TooManySymbolicLinks("Can not open symlink").left()
                } else if (it.fileAttributes.isDirectory) {
                    FileDirectoryHandle.Directory(handle).right()
                } else {
                    FileDirectoryHandle.File(handle, isInAppendMode).right()
                }
            }
    }
}

private fun getCreateDisposition(
    @OpenFileFlagsType flags: OpenFileFlags,
    isDirectoryOrPathRequest: Boolean,
): Int {
    if (isDirectoryOrPathRequest) {
        return FILE_OPEN
    }

    val mayCreateFile = flags and OpenFileFlag.O_CREAT == OpenFileFlag.O_CREAT
    val truncate = flags and OpenFileFlag.O_TRUNC == OpenFileFlag.O_TRUNC

    return if (mayCreateFile) {
        val createFileIfExists = flags and OpenFileFlag.O_EXCL == OpenFileFlag.O_EXCL
        if (createFileIfExists) {
            FILE_CREATE
        } else {
            if (truncate) {
                FILE_OVERWRITE_IF
            } else {
                FILE_OPEN_IF
            }
        }
    } else {
        when {
            truncate -> FILE_OVERWRITE
            else -> FILE_OPEN
        }
    }
}

private fun getDesiredAccess(
    @OpenFileFlagsType flags: OpenFileFlags,
    isDirectoryOrPathRequest: Boolean,
): Int {
    return if (isDirectoryOrPathRequest) {
        FILE_LIST_DIRECTORY or FILE_READ_ATTRIBUTES or FILE_TRAVERSE or FILE_WRITE_ATTRIBUTES
    } else {
        when (flags and O_ACCMODE) {
            O_RDONLY -> FILE_GENERIC_READ
            O_WRONLY -> FILE_GENERIC_WRITE or FILE_READ_ATTRIBUTES
            else -> FILE_GENERIC_READ or FILE_GENERIC_WRITE
        }
    }
}

private fun getFileAttributes(
    @OpenFileFlagsType flags: OpenFileFlags,
    isDirectoryOrPathRequest: Boolean,
): Int {
    if (isDirectoryOrPathRequest) {
        return FILE_ATTRIBUTE_DIRECTORY
    }

    var attrs = 0
    if (flags and O_TMPFILE == O_TMPFILE) {
        attrs = attrs or FILE_ATTRIBUTE_TEMPORARY
    }

    return if (attrs != 0) {
        attrs
    } else {
        FILE_ATTRIBUTE_NORMAL
    }
}

private fun getCreateOptions(
    @FdflagsType fdFlags: Fdflags,
    isDirectoryOrPathRequest: Boolean,
    followSymlinks: Boolean,
): Int {
    var flags = if (!followSymlinks) {
        FILE_OPEN_REPARSE_POINT
    } else {
        0
    }
    if (isDirectoryOrPathRequest) {
        return flags or FILE_DIRECTORY_FILE or FILE_OPEN_FOR_BACKUP_INTENT
    }

    flags = flags or FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT

    if (fdFlags and (FD_DSYNC or FD_SYNC or FD_RSYNC) != 0) {
        flags = flags or FILE_WRITE_THROUGH
    }
    return flags
}

private fun StatError.toOpenError(): OpenError = if (this is OpenError) {
    this
} else {
    IoError("Can not get stat of file handle: $this")
}
