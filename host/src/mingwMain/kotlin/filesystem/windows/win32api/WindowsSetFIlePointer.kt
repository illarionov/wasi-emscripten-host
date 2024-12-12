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
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.model.Whence.CUR
import at.released.weh.filesystem.model.Whence.END
import at.released.weh.filesystem.model.Whence.SET
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.CValue
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FILE_BEGIN
import platform.windows.FILE_CURRENT
import platform.windows.FILE_END
import platform.windows.HANDLE
import platform.windows.LARGE_INTEGER
import platform.windows.SetFilePointerEx

internal fun HANDLE.getFilePointer(): Either<SeekError, Long> = setFilePointer(0, CUR)

internal fun HANDLE.setFilePointer(
    fileDelta: Long,
    whence: Whence,
): Either<SeekError, Long> = memScoped {
    val distanceToMove: CValue<LARGE_INTEGER> = cValue { QuadPart = fileDelta }
    val newFilePointer: LARGE_INTEGER = alloc()

    return if (SetFilePointerEx(this@setFilePointer, distanceToMove, newFilePointer.ptr, whence.asMoveMethod) != 0) {
        newFilePointer.QuadPart.right()
    } else {
        Win32ErrorCode.getLast().toSeekError().left()
    }
}

private fun Win32ErrorCode.toSeekError(): SeekError = when (this.code.toInt()) {
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid file delta")
    else -> InvalidArgument("Other error: `$this`")
}

private val Whence.asMoveMethod: UInt get() = when (this) {
    SET -> FILE_BEGIN
    CUR -> FILE_CURRENT
    END -> FILE_END
}.toUInt()
