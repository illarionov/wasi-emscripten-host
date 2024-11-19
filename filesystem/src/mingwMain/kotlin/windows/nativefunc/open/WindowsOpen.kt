/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.OpenError
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
import at.released.weh.filesystem.windows.win32api.ntCreateFileEx
import platform.posix.O_CREAT
import platform.posix.O_EXCL
import platform.posix.O_TRUNC
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_ATTRIBUTE_READONLY
import platform.windows.FILE_ATTRIBUTE_TEMPORARY
import platform.windows.FILE_CREATE
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_GENERIC_READ
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_IF
import platform.windows.FILE_OVERWRITE
import platform.windows.FILE_OVERWRITE_IF
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_THROUGH
import platform.windows.HANDLE

// TODO: inline?
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
        if (flags and O_CREAT == O_CREAT) {
            return InvalidArgument("O_CREAT cannot be used to create directories").left()
        }
        if (flags and O_ACCMODE != O_RDONLY) {
            return InvalidArgument("Directory should be opened in read/only").left()
        }
    }

    // TODO: validate windows path
    // TODO: nofollow
    validatePath(path).bind()

    val desiredAccess = getDesiredAccess(flags, isDirectoryOrPathRequest)
    val fileAttributes = getFileAttributes(flags, isDirectoryOrPathRequest)
    val createDisposition = getCreateDisposition(flags, isDirectoryRequest)
    val createOptions = getCreateOptions(fdFlagsNoAppend, isDirectoryOrPathRequest)
    val followSymlinks = flags and O_NOFOLLOW != O_NOFOLLOW

    val handle: HANDLE = ntCreateFileEx(
        rootHandle = baseHandle,
        path = path,
        desiredAccess = desiredAccess,
        fileAttributes = fileAttributes,
        createDisposition = createDisposition,
        createOptions = createOptions,
        followSymlinks = followSymlinks,
    ).bind()

    // TODO: read file type
    return if (isDirectoryOrPathRequest) {
        FileDirectoryHandle.Directory(handle)
    } else {
        FileDirectoryHandle.File(handle, isInAppendMode)
    }.right()
}

private fun getCreateDisposition(
    @OpenFileFlagsType flags: OpenFileFlags,
    isDirectoryOrPathRequest: Boolean,
): Int {
    if (isDirectoryOrPathRequest) {
        return FILE_OPEN
    }

    val mayCreateFile = flags and O_CREAT == O_CREAT
    val truncate = flags and O_TRUNC == O_TRUNC

    return if (mayCreateFile) {
        val createFailIfExists = flags and O_EXCL == O_EXCL
        if (createFailIfExists) {
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
        FILE_LIST_DIRECTORY or FILE_TRAVERSE
    } else {
        when (flags and O_ACCMODE) {
            O_RDONLY -> FILE_GENERIC_READ
            O_WRONLY -> FILE_GENERIC_WRITE
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
    if (flags and O_ACCMODE == O_RDONLY) {
        attrs = attrs or FILE_ATTRIBUTE_READONLY
    }
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
): Int {
    if (isDirectoryOrPathRequest) {
        return FILE_DIRECTORY_FILE
    }
    var flags = FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT
    if (fdFlags and (FD_DSYNC or FD_SYNC or FD_RSYNC) != 0) {
        flags = flags or FILE_WRITE_THROUGH
    }
    return flags
}
