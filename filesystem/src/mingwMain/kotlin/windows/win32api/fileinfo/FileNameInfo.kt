/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.windows.ERROR_MORE_DATA
import platform.windows.FILE_NAME_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows.MAX_PATH
import platform.windows.WCHARVar
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
internal fun windowsGetFileFilename(handle: HANDLE): Either<StatError, String> {
    var maxLength = MAX_PATH * sizeOf<WCHARVar>()
    val fnSize = sizeOf<FILE_NAME_INFO>()
    val fnAlign = alignOf<FILE_NAME_INFO>()
    repeat(3) {
        memScoped {
            val totalSize = fnSize + maxLength
            val fileNameInfoBuf = alloc(totalSize, fnAlign)
            val filenameInfo: FILE_NAME_INFO = fileNameInfoBuf.reinterpret()

            val result = GetFileInformationByHandleEx(
                handle,
                _FILE_INFO_BY_HANDLE_CLASS.FileNameInfo,
                filenameInfo.ptr,
                totalSize.toUInt(),
            )

            if (result != 0) {
                // Name is not null-terminated according to MS-FSCC
                val buf = filenameInfo.FileName.readChars(
                    (filenameInfo.FileNameLength / sizeOf<WCHARVar>().toULong()).toInt(),
                )
                return buf.concatToString().right()
            } else {
                val errCode = Win32ErrorCode.getLast()
                if (errCode.code != ERROR_MORE_DATA.toUInt()) {
                    return Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
                } else {
                    maxLength = filenameInfo.FileNameLength.toLong()
                }
            }
        }
    }
    return IoError("Can not get file name: max attempts reached").left()
}
