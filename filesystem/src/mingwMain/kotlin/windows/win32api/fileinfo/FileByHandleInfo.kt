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
import platform.windows.BY_HANDLE_FILE_INFORMATION
import platform.windows.GetFileInformationByHandle
import platform.windows.HANDLE

internal fun windowsGetFileInformationByHandle(handle: HANDLE): Either<StatError, FileByHandleInfo> = memScoped {
    val info: BY_HANDLE_FILE_INFORMATION = alloc()
    val result = GetFileInformationByHandle(
        handle,
        info.ptr,
    )
    return if (result != 0) {
        FileByHandleInfo.create(info).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileByHandleInfo(
    val fileAttributes: FileAttributes,
    val creationTime: StructTimespec,
    val lastAccessTime: StructTimespec,
    val lastWriteTime: StructTimespec,
    val volumeSerialNumber: UInt,
    val size: Long,
    val numberOfLinks: UInt,
    val fileIndex: Long,
) {
    internal companion object {
        fun create(
            info: BY_HANDLE_FILE_INFORMATION,
        ): FileByHandleInfo = FileByHandleInfo(
            fileAttributes = FileAttributes(info.dwFileAttributes),
            creationTime = info.ftCreationTime.asStructTimespec,
            lastAccessTime = info.ftLastAccessTime.asStructTimespec,
            lastWriteTime = info.ftLastWriteTime.asStructTimespec,
            volumeSerialNumber = info.dwVolumeSerialNumber,
            size = qwordsToULong(info.nFileSizeHigh, info.nFileSizeLow).toLong(),
            numberOfLinks = info.nNumberOfLinks,
            fileIndex = qwordsToULong(info.nFileIndexHigh, info.nFileIndexLow).toLong(),
        )

        private fun qwordsToULong(qHigh: UInt, qLow: UInt): ULong =
            ((qHigh.toULong() shl 32) or (qLow.toULong() and 0xFF_FF_FF_FF_UL))
    }
}
