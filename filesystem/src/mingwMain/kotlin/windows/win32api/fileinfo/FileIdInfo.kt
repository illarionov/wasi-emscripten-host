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
import at.released.weh.filesystem.platform.windows.FILE_ID_INFO
import at.released.weh.filesystem.platform.windows.GetFileInformationByHandleEx
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.ext.asByteString
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.io.bytestring.ByteString
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.HANDLE

internal fun HANDLE.getFileIdInfo(): Either<StatError, FileIdInfo> = memScoped {
    val info: FILE_ID_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileIdInfo,
        FILE_INFO_BY_HANDLE_CLASS.FileIdInfo,
        info.ptr,
        sizeOf<FILE_ID_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileIdInfo.create(info).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileIdInfo(
    val volumeSerialNumber: ULong,
    val fileId: ByteString,
) {
    init {
        check(fileId.size == 16)
    }

    internal companion object {
        fun create(info: FILE_ID_INFO) = FileIdInfo(
            volumeSerialNumber = info.VolumeSerialNumber,
            fileId = info.FileId.asByteString(),
        )
    }
}
