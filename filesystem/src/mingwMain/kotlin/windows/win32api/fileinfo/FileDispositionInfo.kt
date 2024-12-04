/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_DISPOSITION_INFO
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.HANDLE
import platform.windows.SetFileInformationByHandle

internal fun HANDLE.setFileDispositionInfo(
    deleteFile: Boolean,
): Either<Win32ErrorCode, Unit> = memScoped {
    val fileDispositionInfo: FILE_DISPOSITION_INFO = alloc<FILE_DISPOSITION_INFO> {
        this.DeleteFileW = if (deleteFile) 1.toUByte() else 0.toUByte()
    }
    val result = SetFileInformationByHandle(
        this@setFileDispositionInfo,
        FILE_INFO_BY_HANDLE_CLASS.FileDispositionInfo,
        fileDispositionInfo.ptr,
        sizeOf<FILE_DISPOSITION_INFO>().toUInt(),
    )
    return if (result != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().left()
    }
}
