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
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.ext.fromAttributes
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.ReparseTag
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_ATTRIBUTE_TAG_INFO
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE

internal fun HANDLE.getFileAttributeTagInfo(): Either<StatError, FileAttributeTagInfo> = memScoped {
    val fileAttributeTagInfo: FILE_ATTRIBUTE_TAG_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileAttributeTagInfo,
        FILE_INFO_BY_HANDLE_CLASS.FileAttributeTagInfo,
        fileAttributeTagInfo.ptr,
        sizeOf<FILE_ATTRIBUTE_TAG_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileAttributeTagInfo(
            FileAttributes(fileAttributeTagInfo.FileAttributes),
            ReparseTag(fileAttributeTagInfo.ReparseTag),
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileAttributeTagInfo(
    val fileAttributes: FileAttributes,
    val reparseTag: ReparseTag,
) {
    val isSymlink = fileAttributes.isSymlinkOrReparsePoint && reparseTag.isSymlink
}

internal val FileAttributeTagInfo.filetype: Filetype get() = Filetype.fromAttributes(fileAttributes, reparseTag)
