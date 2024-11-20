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
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_FULL_DIR_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileFullDirectoryInfo
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileFullDirectoryRestartInfo

internal fun windowsGetFileIdFullDirectoryInfo(
    handle: HANDLE,
    restart: Boolean = false,
    bufferSize: Int = 64 * 1024 * 1024,
): Either<StatError, List<FileFullDirInfo>> = memScoped {
    val buffer = alloc(bufferSize, alignOf<FILE_FULL_DIR_INFO>())
    val firstId: FILE_FULL_DIR_INFO = buffer.reinterpret()

    val infoClass = if (restart) {
        FileFullDirectoryRestartInfo
    } else {
        FileFullDirectoryInfo
    }

    val result = GetFileInformationByHandleEx(handle, infoClass, firstId.ptr, bufferSize.toUInt())
    return if (result != 0) {
        readListOfItemsByNextEntryOffset(
            buffer,
            bufferSize,
            FileFullDirInfo::create,
            FileFullDirInfo::nextEntryOffset,
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileFullDirInfo(
    val nextEntryOffset: UInt,
    val fileIndex: UInt,
    val creationTime: StructTimespec,
    val lastAccessTime: StructTimespec,
    val lastWriteTime: StructTimespec,
    val changeTime: StructTimespec,
    val endOfFile: Long,
    val allocationSize: Long,
    val fileAttributes: UInt,
    val eaSize: UInt,
    val filename: String,
) {
    internal companion object {
        fun create(
            info: FILE_FULL_DIR_INFO,
            maxBytes: Int,
        ): FileFullDirInfo? {
            @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
            val structSize = sizeOf<FILE_FULL_DIR_INFO>()
            if (maxBytes < structSize) {
                return null
            }
            val totalSize = structSize + (info.FileNameLength.toInt() * 2) - 2
            if (maxBytes < totalSize) {
                return null
            }

            return FileFullDirInfo(
                nextEntryOffset = info.NextEntryOffset,
                fileIndex = info.FileIndex,
                creationTime = info.CreationTime.asStructTimespec,
                lastAccessTime = info.LastAccessTime.asStructTimespec,
                lastWriteTime = info.LastAccessTime.asStructTimespec,
                changeTime = info.ChangeTime.asStructTimespec,
                endOfFile = info.EndOfFile.QuadPart,
                allocationSize = info.AllocationSize.QuadPart,
                fileAttributes = info.FileAttributes,
                eaSize = info.EaSize,
                filename = info.FileName.readChars(info.FileNameLength.toInt()).concatToString(),
            )
        }
    }
}
