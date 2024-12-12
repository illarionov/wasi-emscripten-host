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
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.host.platform.windows.FILE_ALIGNMENT_INFO
import at.released.weh.host.platform.windows.GetFileInformationByHandleEx
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

internal fun HANDLE.getFileAlignmentInfo(): Either<StatError, UInt> = memScoped {
    val info: FILE_ALIGNMENT_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileAlignmentInfo,
        _FILE_INFO_BY_HANDLE_CLASS.FileAlignmentInfo,
        info.ptr,
        sizeOf<FILE_ALIGNMENT_INFO>().toUInt(),
    )
    return if (result != 0) {
        info.AlignmentRequirement.right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}
