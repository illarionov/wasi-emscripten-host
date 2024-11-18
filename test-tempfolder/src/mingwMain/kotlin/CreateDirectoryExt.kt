/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.io.IOException
import platform.windows.CreateDirectoryW
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_PATH_NOT_FOUND
import platform.windows.GetLastError

@Throws(IOException::class)
internal fun createDirectory(path: String): Boolean {
    if (CreateDirectoryW(path, null) != 0) {
        return true
    }

    val lastError = GetLastError()
    if (lastError == ERROR_ALREADY_EXISTS.toUInt()) {
        return false
    } else {
        throw createDirectoryToWindowsIoException(lastError)
    }
}

private fun createDirectoryToWindowsIoException(lastError: UInt): WindowsIoException = when (lastError.toInt()) {
    ERROR_ALREADY_EXISTS -> WindowsIoException("Path already exists", lastError)
    ERROR_PATH_NOT_FOUND -> WindowsIoException("Failed to resolve intermediate directories", lastError)
    else -> WindowsIoException(
        "Windows error. Code: 0x${lastError.toString(16).padStart(8, '0')}`",
        lastError,
    )
}
