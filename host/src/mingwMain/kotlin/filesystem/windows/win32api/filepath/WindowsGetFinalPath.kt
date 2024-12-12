/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.filepath

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
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
): Either<GetFinalPathError, WindowsRealPath> {
    val flags = getPathFlags(volumeNameType, normalized)
    var requiredBuffer: Int = MAX_PATH
    repeat(MAX_ATTEMPTS) {
        memScoped {
            val buf: CPointer<WCHARVar> = allocArray(requiredBuffer)
            val result = GetFinalPathNameByHandleW(this@getFinalPath, buf, requiredBuffer.toUInt(), flags)
            when {
                result == 0U -> return GetFinalPathError.create(Win32ErrorCode.getLast()).left()
                result.toInt() < requiredBuffer -> {
                    val pathString = buf.readChars(result.toInt()).concatToString()
                    return WindowsRealPath.create(pathString)
                        .mapLeft { GetFinalPathError.InvalidPathFormat(it.message) }
                }

                else -> requiredBuffer = result.toInt()
            }
        }
    }
    return GetFinalPathError.MaxAttemptsReached("Can not get path: max attempts reached").left()
}

internal fun GetFinalPathError.toResolveRelativePathError(): ResolveRelativePathErrors = when (this) {
    is GetFinalPathError.AccessDenied -> NotCapable(this.message)
    is GetFinalPathError.InvalidHandle -> BadFileDescriptor(this.message)
    is GetFinalPathError.MaxAttemptsReached -> NameTooLong(this.message)
    is GetFinalPathError.OtherError -> InvalidArgument(this.message)
    is GetFinalPathError.InvalidPathFormat -> InvalidArgument(this.message)
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
