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
import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.windows.win32api.ext.asStructTimespec
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_BASIC_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

internal fun HANDLE.getFileBasicInfo(): Either<StatError, FileBasicInfo> = memScoped {
    val basicInfo: FILE_BASIC_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileBasicInfo,
        _FILE_INFO_BY_HANDLE_CLASS.FileBasicInfo,
        basicInfo.ptr,
        sizeOf<FILE_BASIC_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileBasicInfo.create(basicInfo).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileBasicInfo(
    val creationTime: StructTimespec,
    val lastAccessTime: StructTimespec,
    val lastWriteTime: StructTimespec,
    val changeTime: StructTimespec,
    val fileAttributes: FileAttributes,
) {
    internal companion object {
        fun create(
            info: FILE_BASIC_INFO,
        ): FileBasicInfo = FileBasicInfo(
            creationTime = info.CreationTime.asStructTimespec,
            lastAccessTime = info.LastAccessTime.asStructTimespec,
            lastWriteTime = info.LastWriteTime.asStructTimespec,
            changeTime = info.ChangeTime.asStructTimespec,
            fileAttributes = FileAttributes(info.FileAttributes),
        )
    }
}
