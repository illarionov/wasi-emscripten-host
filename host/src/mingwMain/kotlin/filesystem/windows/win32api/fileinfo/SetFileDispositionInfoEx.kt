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
import at.released.weh.host.platform.windows.FILE_DISPOSITION_INFORMATION_EX
import at.released.weh.host.platform.windows.SetFileInformationByHandle
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.HANDLE

private const val FILE_DISPOSITION_DO_NOT_DELETE = 0x0U
private const val FILE_DISPOSITION_DELETE = 0x1U
private const val FILE_DISPOSITION_POSIX_SEMANTICS = 0x2U
private const val FILE_DISPOSITION_IGNORE_READONLY_ATTRIBUTE = 0x10U

internal fun HANDLE.setFileDispositionInfoEx(
    deleteFile: Boolean,
): Either<Win32ErrorCode, Unit> = memScoped {
    val fileDispositionInfo: FILE_DISPOSITION_INFORMATION_EX = alloc<FILE_DISPOSITION_INFORMATION_EX> {
        this.Flags = if (deleteFile) {
            FILE_DISPOSITION_DELETE or FILE_DISPOSITION_POSIX_SEMANTICS or FILE_DISPOSITION_IGNORE_READONLY_ATTRIBUTE
        } else {
            FILE_DISPOSITION_DO_NOT_DELETE
        }
    }
    val result = SetFileInformationByHandle(
        this@setFileDispositionInfoEx,
        FILE_INFO_BY_HANDLE_CLASS.FileDispositionInfoEx,
        fileDispositionInfo.ptr,
        sizeOf<FILE_DISPOSITION_INFORMATION_EX>().toUInt(),
    )
    return if (result != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().left()
    }
}
