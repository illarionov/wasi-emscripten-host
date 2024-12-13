/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FILE_ALLOCATION_INFO
import platform.windows.HANDLE
import platform.windows.SetFileInformationByHandle
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileAllocationInfo

internal fun HANDLE.setFileAllocationSize(size: Long): Either<FallocateError, Unit> = memScoped {
    val info: FILE_ALLOCATION_INFO = alloc<FILE_ALLOCATION_INFO>().apply {
        AllocationSize.QuadPart = size
    }
    val result = SetFileInformationByHandle(
        this@setFileAllocationSize,
        FileAllocationInfo,
        info.ptr,
        sizeOf<FILE_ALLOCATION_INFO>().toUInt(),
    )

    return if (result != 0) {
        Unit.right()
    } else {
        Win32ErrorCode.getLast().toFallocateError().left()
    }
}

private fun Win32ErrorCode.toFallocateError(): FallocateError = when (this.code.toInt()) {
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect file pointer position")
    // TODO: find error codes for ENOSPC, EFBIG.
    else -> InvalidArgument("Other error: `$this`")
}
