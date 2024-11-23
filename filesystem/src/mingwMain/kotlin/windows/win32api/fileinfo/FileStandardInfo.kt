/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_STANDARD_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

internal fun HANDLE.getFileStandardInfo(): Either<StatError, FileStandardInfo> = memScoped {
    val standardInfo: FILE_STANDARD_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileStandardInfo,
        _FILE_INFO_BY_HANDLE_CLASS.FileStandardInfo,
        standardInfo.ptr,
        sizeOf<FILE_STANDARD_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileStandardInfo.create(standardInfo).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileStandardInfo(
    val allocationSize: Long,
    val endOfFile: Long,
    val numberOfLinks: UInt,
    val isDeletePending: Boolean,
    val isDirectory: Boolean,
) {
    internal companion object {
        fun create(
            info: FILE_STANDARD_INFO,
        ): FileStandardInfo = FileStandardInfo(
            allocationSize = info.AllocationSize.QuadPart,
            endOfFile = info.EndOfFile.QuadPart,
            numberOfLinks = info.NumberOfLinks,
            isDeletePending = info.DeletePending.toInt() != 0,
            isDirectory = info.Directory.toInt() != 0,
        )
    }
}
