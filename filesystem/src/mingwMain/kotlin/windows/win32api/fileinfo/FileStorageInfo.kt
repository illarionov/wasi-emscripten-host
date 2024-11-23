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
import at.released.weh.filesystem.platform.windows.FILE_STORAGE_INFO
import at.released.weh.filesystem.platform.windows.GetFileInformationByHandleEx
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.windows.HANDLE
import platform.windows._FILE_INFO_BY_HANDLE_CLASS

internal fun HANDLE.getFileStorageInfo(): Either<StatError, FileStorageInfo> = memScoped {
    val info: FILE_STORAGE_INFO = alloc()
    val result = GetFileInformationByHandleEx(
        this@getFileStorageInfo,
        _FILE_INFO_BY_HANDLE_CLASS.FileStorageInfo,
        info.ptr,
        sizeOf<FILE_STORAGE_INFO>().toUInt(),
    )
    return if (result != 0) {
        FileStorageInfo.create(info).right()
    } else {
        Win32ErrorCode.getLast().getFileInfoErrorToStatError().left()
    }
}

internal data class FileStorageInfo(
    val logicalBytesPerSector: UInt,
    val physicalBytesPerSectorForAtomicity: UInt,
    val physicalBytesPerSectorForPerformance: UInt,
    val fileSystemEffectivePhysicalBytesPerSectorForAtomicity: UInt,
    val flags: UInt,
    val byteOffsetForSectorAlignment: UInt,
    val byteOffsetForPartitionAlignment: UInt,
) {
    internal companion object {
        @Suppress("MaxLineLength")
        fun create(info: FILE_STORAGE_INFO): FileStorageInfo = FileStorageInfo(
            logicalBytesPerSector = info.LogicalBytesPerSector,
            physicalBytesPerSectorForAtomicity = info.PhysicalBytesPerSectorForAtomicity,
            physicalBytesPerSectorForPerformance = info.PhysicalBytesPerSectorForPerformance,
            fileSystemEffectivePhysicalBytesPerSectorForAtomicity = info.FileSystemEffectivePhysicalBytesPerSectorForAtomicity,
            flags = info.Flags,
            byteOffsetForSectorAlignment = info.ByteOffsetForSectorAlignment,
            byteOffsetForPartitionAlignment = info.ByteOffsetForPartitionAlignment,
        )
    }
}
