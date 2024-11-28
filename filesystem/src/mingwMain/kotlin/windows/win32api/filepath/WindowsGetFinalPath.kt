/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.filepath

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.filesystem.windows.win32api.filepath.WindowsVolumeNameType.DOS
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import platform.windows.FILE_NAME_NORMALIZED
import platform.windows.FILE_NAME_OPENED
import platform.windows.GetFinalPathNameByHandleW
import platform.windows.HANDLE
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

private const val MAX_ATTEMPTS = 10

internal fun HANDLE.getFinalPath(
    volumeNameType: WindowsVolumeNameType = DOS,
    normalized: Boolean = true,
): Either<GetFinalPathError, String> {
    val flags = getPathFlags(volumeNameType, normalized)
    var requiredBuffer: Int = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        memScoped {
            val buf: CPointer<WCHARVar> = allocArray(requiredBuffer)
            val result = GetFinalPathNameByHandleW(this@getFinalPath, buf, requiredBuffer.toUInt(), flags)
            when {
                result == 0U -> return GetFinalPathError.create(Win32ErrorCode.getLast()).left()
                result.toInt() < requiredBuffer -> return buf.readChars(result.toInt()).concatToString().right()
                else -> requiredBuffer = result.toInt()
            }
        }
    }
    return GetFinalPathError.MaxAttemptsReached("Can not get path: max attempts reached").left()
}

private fun getPathFlags(
    volumeNameType: WindowsVolumeNameType,
    normalized: Boolean = true,
): UInt {
    val normalizedMask: Int = if (normalized) {
        FILE_NAME_NORMALIZED
    } else {
        FILE_NAME_OPENED
    }
    return (normalizedMask or volumeNameType.win32Mask).toUInt()
}
