/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.windows.win32api.ext.asStructTimespec
import at.released.weh.filesystem.windows.win32api.ext.readChars
import kotlinx.cinterop.sizeOf
import platform.windows.FILE_ID_BOTH_DIR_INFO

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
                shortname = info.ShortName.readChars(info.ShortNameLength.toInt().coerceAtMost(12)).concatToString(),
                fileId = info.FileId.QuadPart,
                filename = info.FileName.readChars(info.FileNameLength.toInt()).concatToString(),
            )
        }
    }
}
