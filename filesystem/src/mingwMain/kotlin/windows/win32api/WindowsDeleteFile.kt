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
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.DeleteFileW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER

internal fun windowsDeleteFile(
    path: RealPath,
): Either<UnlinkError, Unit> {
    return if (DeleteFileW(path) != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().deleteFileErrorToUnlinkError().left()
    }
}

private fun Win32ErrorCode.deleteFileErrorToUnlinkError(): UnlinkError = when (this.code.toInt()) {
    // TODO: find error codes
    ERROR_ACCESS_DENIED -> AccessDenied("Cannot delete file, access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("File not found")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect path")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    else -> InvalidArgument("Other error `$this`")
}
