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
import platform.windows.FILE_ID_BOTH_DIR_INFO
import platform.windows.GetFileInformationByHandleEx
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileIdBothDirectoryInfo
import platform.windows._FILE_INFO_BY_HANDLE_CLASS.FileIdBothDirectoryRestartInfo

private const val WINDOWS_8_3_FILENAME_MAX_LENGTH = 12

internal fun HANDLE.getFileIdBothDirectoryInfo(
    restart: Boolean = false,
    bufferSize: Int = 64 * 1024 * 1024,
): Either<StatError, List<FileIdBothDirInfo>> = memScoped {
    val buffer = alloc(bufferSize, alignOf<FILE_ID_BOTH_DIR_INFO>())
    val firstId: FILE_ID_BOTH_DIR_INFO = buffer.reinterpret()

    val infoClass = if (restart) {
        FileIdBothDirectoryRestartInfo
    } else {
        FileIdBothDirectoryInfo
    }

    val result = GetFileInformationByHandleEx(
        hFile = this@getFileIdBothDirectoryInfo,
        FileInformationClass = infoClass,
        lpFileInformation = firstId.ptr,
        dwBufferSize = bufferSize.toUInt(),
    )
    return if (result != 0) {
        readListOfItemsByNextEntryOffset(
            buffer = buffer,
            bufferSize = bufferSize,
            itemFactory = FileIdBothDirInfo::create,
            nextEntryOffset = FileIdBothDirInfo::nextEntryOffset,
        ).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileIdBothDirInfo(
    val nextEntryOffset: UInt,
    val fileIndex: UInt,
    val creationTime: StructTimespec,
    val lastAccessTime: StructTimespec,
    val lastWriteTime: StructTimespec,
    val changeTime: StructTimespec,
    val endOfFile: Long,
    val fileAttributes: UInt,
    val eaSize: UInt,
    val shortname: String,
    val fileId: Long,
    val filename: String,
) {
    internal companion object {
        fun create(
            info: FILE_ID_BOTH_DIR_INFO,
            maxBytes: Int,
        ): FileIdBothDirInfo? {
            @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
            val structSize = sizeOf<FILE_ID_BOTH_DIR_INFO>()
            if (maxBytes < structSize) {
                return null
            }
            val totalSize = structSize + (info.FileNameLength.toInt() * 2) - 2
            if (maxBytes < totalSize) {
                return null
            }

            return FileIdBothDirInfo(
                nextEntryOffset = info.NextEntryOffset,
                fileIndex = info.FileIndex,
                creationTime = info.CreationTime.asStructTimespec,
                lastAccessTime = info.LastAccessTime.asStructTimespec,
                lastWriteTime = info.LastAccessTime.asStructTimespec,
                changeTime = info.ChangeTime.asStructTimespec,
                endOfFile = info.EndOfFile.QuadPart,
                fileAttributes = info.FileAttributes,
                eaSize = info.EaSize,
                shortname = info.ShortName.readChars(
                    info.ShortNameLength.toInt().coerceAtMost(WINDOWS_8_3_FILENAME_MAX_LENGTH),
                ).concatToString(),
                fileId = info.FileId.QuadPart,
                filename = info.FileName.readChars(info.FileNameLength.toInt()).concatToString(),
            )
        }
    }
}
