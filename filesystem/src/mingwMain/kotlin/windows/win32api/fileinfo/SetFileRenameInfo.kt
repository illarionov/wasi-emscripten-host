/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.CValues
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.utf16
import platform.windows.FILE_INFO_BY_HANDLE_CLASS
import platform.windows.FILE_RENAME_INFO
import platform.windows.HANDLE
import platform.windows.SetFileInformationByHandle

internal fun HANDLE.setFileRenameInfo(
    newName: RealPath,
    replaceIfExists: Boolean = false,
): Either<Win32ErrorCode, Unit> = memScoped {
    val nameBytes: CValues<UShortVar> = newName.utf16
    val fullSize = sizeOf<FILE_RENAME_INFO>() + nameBytes.size
    val renameBuffer = alloc(fullSize, alignOf<FILE_RENAME_INFO>())

    val renameInfo = renameBuffer.reinterpret<FILE_RENAME_INFO>().apply {
        ReplaceIfExists = if (replaceIfExists) 1.toUByte() else 0.toUByte()
        RootDirectory = null
        FileNameLength = (nameBytes.size - 2).toUInt()
        nameBytes.place(FileName)
    }

    val result = SetFileInformationByHandle(
        this@setFileRenameInfo,
        FILE_INFO_BY_HANDLE_CLASS.FileRenameInfo,
        renameInfo.ptr,
        fullSize.toUInt(),
    )
    return if (result != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().left()
    }
}
