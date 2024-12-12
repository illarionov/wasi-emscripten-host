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
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.CreateHardLinkW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_DIRECTORY
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER

internal fun windowsCreateHardLink(
    newPath: WindowsRealPath,
    oldPath: WindowsRealPath,
): Either<HardlinkError, Unit> {
    return if (CreateHardLinkW(newPath.kString, oldPath.kString, null) != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().toHardLinkError().left()
    }
}

private fun Win32ErrorCode.toHardLinkError(): HardlinkError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Cannot unlink directory, access denied")
    ERROR_ALREADY_EXISTS -> Exists("Cannot create a file when that file already exists")
    ERROR_DIRECTORY -> NotDirectory("Invalid directory name")
    ERROR_FILE_NOT_FOUND -> NotDirectory("Directory not found")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect directory path")
    else -> InvalidArgument("Other error `$this`")
}
