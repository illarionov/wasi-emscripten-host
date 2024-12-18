/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.HANDLE
import platform.windows.SetEndOfFile

internal fun HANDLE.setEndOfFile(): Either<TruncateError, Unit> {
    return if (SetEndOfFile(this) != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().toTruncateError().left()
    }
}

private fun Win32ErrorCode.toTruncateError(): TruncateError = when (this.code.toInt()) {
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect file pointer position")
    // XXX: find error codes for ENOSPC, EFBIG.
    else -> InvalidArgument("Other error: `$this`")
}
