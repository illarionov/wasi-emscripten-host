/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.platform.windows.FILE_ID_EXTD_DIR_INFO
import at.released.weh.filesystem.windows.win32api.ext.asByteString
import at.released.weh.filesystem.windows.win32api.ext.asStructTimespec
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.filesystem.windows.win32api.model.ReparseTag
import kotlinx.cinterop.sizeOf
import kotlinx.io.bytestring.ByteString

internal data class FileIdExtdDirInfo(
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
    val reparsePointTag: ReparseTag,
    val fileId: ByteString,
    val filename: String,
) {
    init {
        check(fileId.size == 16)
    }

    internal companion object {
        fun create(
            info: FILE_ID_EXTD_DIR_INFO,
            maxBytes: Int,
        ): FileIdExtdDirInfo? {
            val structSize = sizeOf<FILE_ID_EXTD_DIR_INFO>()
            if (maxBytes < structSize) {
                return null
            }
            val totalSize = structSize + (info.FileNameLength.toInt() * 2) - 2
            if (maxBytes < totalSize) {
                return null
            }

            return FileIdExtdDirInfo(
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
                reparsePointTag = ReparseTag(info.ReparsePointTag),
                fileId = info.FileId.asByteString(),
                filename = info.FileName.readChars(info.FileNameLength.toInt()).concatToString(),
            )
        }
    }
}
