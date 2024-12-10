/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readdir

import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER

internal fun StatError.toReadDirError(): ReadDirError = if (this is ReadDirError) {
    this
} else {
    IoError(this.message)
}

internal fun Win32ErrorCode.toReadDirError(): ReadDirError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("file not found")
    ERROR_INVALID_PARAMETER -> IoError("Invalid parameters")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    else -> IoError("Other error: $this")
}

internal fun OpenError.toReadDirError(): ReadDirError = if (this is ReadDirError) {
    this
} else {
    IoError(this.message)
}

internal fun GetFinalPathError.toReadDirError(): ReadDirError = when (this) {
    is GetFinalPathError.AccessDenied -> AccessDenied(this.message)
    is GetFinalPathError.InvalidHandle -> BadFileDescriptor(this.message)
    is GetFinalPathError.MaxAttemptsReached -> IoError(this.message)
    is GetFinalPathError.OtherError -> IoError(this.message)
    is GetFinalPathError.InvalidPathFormat -> IoError(this.message)
}
