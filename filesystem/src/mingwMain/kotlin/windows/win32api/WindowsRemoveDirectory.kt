/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DirectoryNotEmpty
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_DIRECTORY
import platform.windows.ERROR_DIR_NOT_EMPTY
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_NOT_EMPTY
import platform.windows.RemoveDirectoryW

internal fun windowsRemoveDirectory(
    path: RealPath,
): Either<UnlinkError, Unit> {
    return if (RemoveDirectoryW(path) != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().removeDirectoryErrorToUnlinkError().left()
    }
}

private fun Win32ErrorCode.removeDirectoryErrorToUnlinkError(): UnlinkError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Cannot unlink directory, access denied")
    ERROR_FILE_NOT_FOUND -> NotDirectory("Directory not found")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect directory path")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_NOT_EMPTY, ERROR_DIR_NOT_EMPTY -> DirectoryNotEmpty("Directory not empty")
    ERROR_DIRECTORY -> NotDirectory("Invalid directory name")
    else -> InvalidArgument("Other error `$this`")
}
