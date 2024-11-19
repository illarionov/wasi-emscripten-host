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
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import platform.windows.CloseHandle
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.HANDLE

internal fun windowsCloseHandle(
    handle: HANDLE,
): Either<CloseError, Unit> {
    return if (CloseHandle(handle) != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().toCloseError().left()
    }
}

private fun Win32ErrorCode.toCloseError(): CloseError = when (this.code.toInt()) {
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file hande")
    else -> IoError("Other error: `$this`")
}
