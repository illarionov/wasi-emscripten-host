/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")

package at.released.weh.filesystem.windows.win32api.filepath

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_INVALID_HANDLE

internal sealed class GetFinalPathError : FileSystemOperationError {
    internal data class InvalidHandle(override val message: String) : GetFinalPathError() {
        override val errno: FileSystemErrno = FileSystemErrno.BADF
    }

    internal data class AccessDenied(override val message: String) : GetFinalPathError() {
        override val errno: FileSystemErrno = FileSystemErrno.ACCES
    }

    internal data class MaxAttemptsReached(override val message: String) : GetFinalPathError() {
        override val errno: FileSystemErrno = FileSystemErrno.OVERFLOW
    }

    internal data class OtherError(
        val code: Win32ErrorCode,
        override val message: String,
    ) : GetFinalPathError() {
        override val errno: FileSystemErrno = FileSystemErrno.IO
    }

    internal companion object {
        fun create(code: Win32ErrorCode): GetFinalPathError = when (code.code.toInt()) {
            ERROR_ACCESS_DENIED -> GetFinalPathError.AccessDenied("Can not read attributes: access denied")
            ERROR_INVALID_HANDLE -> InvalidHandle("Invalid handle")
            else -> GetFinalPathError.OtherError(code, "Other error `$this`")
        }
    }
}
