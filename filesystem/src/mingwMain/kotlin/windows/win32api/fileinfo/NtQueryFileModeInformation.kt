/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.platform.windows.FILE_MODE_INFORMATION
import at.released.weh.filesystem.platform.windows.FileModeInformation
import at.released.weh.filesystem.platform.windows.IO_STATUS_BLOCK
import at.released.weh.filesystem.platform.windows.NtQueryInformationFile
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus
import at.released.weh.filesystem.windows.win32api.errorcode.isSuccess
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.HANDLE

internal fun HANDLE.ntQueryFileModeInformation(): Either<StatError, Set<FileModeFlag>> = memScoped {
    val info: FILE_MODE_INFORMATION = alloc()
    val ioStatusBlock: IO_STATUS_BLOCK = alloc()

    val ntstatus = NtQueryInformationFile(
        this@ntQueryFileModeInformation,
        ioStatusBlock.ptr,
        info.ptr,
        sizeOf<FILE_MODE_INFORMATION>().toUInt(),
        FileModeInformation,
    ).let { NtStatus(it.toUInt()) }

    return if (ntstatus.isSuccess) {
        info.toFileModeFlag().right()
    } else {
        ntstatus.toStatError().left()
    }
}

private fun FILE_MODE_INFORMATION.toFileModeFlag(): Set<FileModeFlag> = FileModeFlag.entries.filterTo(mutableSetOf()) {
    this@toFileModeFlag.Mode.toInt() and it.mask == it.mask
}

private fun NtStatus.toStatError(): StatError = when (this.raw) {
    NtStatus.STATUS_INVALID_PARAMETER -> InvalidArgument("NtQueryInformationFile failed: invalid argument")
    else -> IoError("Other error $this")
}

internal enum class FileModeFlag(val mask: Int) {
    FILE_WRITE_THROUGH(platform.windows.FILE_WRITE_THROUGH),
    FILE_SEQUENTIAL_ONLY(platform.windows.FILE_SEQUENTIAL_ONLY),
    FILE_NO_INTERMEDIATE_BUFFERING(platform.windows.FILE_NO_INTERMEDIATE_BUFFERING),
    FILE_SYNCHRONOUS_IO_ALERT(platform.windows.FILE_SYNCHRONOUS_IO_ALERT),
    FILE_SYNCHRONOUS_IO_NONALERT(platform.windows.FILE_SYNCHRONOUS_IO_NONALERT),
    FILE_DELETE_ON_CLOSE(platform.windows.FILE_DELETE_ON_CLOSE),
}
