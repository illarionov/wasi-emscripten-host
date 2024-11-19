/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.platform.windows.FILE_STORAGE_INFO

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
