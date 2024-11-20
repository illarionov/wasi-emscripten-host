/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.stat

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXG
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXO
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXU
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.windows.win32api.ext.get64bitInode
import at.released.weh.filesystem.windows.win32api.fileinfo.windowsGetFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.windowsGetFileBasicInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.windowsGetFileIdInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.windowsGetFileStandardInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.windowsGetFileStorageInfo
import at.released.weh.filesystem.windows.win32api.model.FileAttributes.Companion.toFiletype
import platform.windows.HANDLE

private const val DEFAULT_BLOCK_SIZE_BYTES = 512L

internal fun windowsStatFd(
    handle: HANDLE,
): Either<StatError, StructStat> = either {
    val deviceInfo = windowsGetFileStorageInfo(handle).bind()
    val fileId = windowsGetFileIdInfo(handle).bind()
    val basicInfo = windowsGetFileBasicInfo(handle).bind()
    val standardInfo = windowsGetFileStandardInfo(handle).bind()

    val tagInfo = windowsGetFileAttributeTagInfo(handle).bind()
    val fileType = basicInfo.fileAttributes.toFiletype(tagInfo.reparseTag)

    val blockSize: Long = if (deviceInfo.logicalBytesPerSector > 0UL) {
        deviceInfo.logicalBytesPerSector.toLong()
    } else {
        DEFAULT_BLOCK_SIZE_BYTES
    }

    return StructStat(
        deviceId = fileId.volumeSerialNumber.toLong(), // XXX should we not leak serial number?
        inode = fileId.get64bitInode(),
        mode = S_IRWXU or S_IRWXG or S_IRWXO, // XXX guess from Linux rights or read on WSL?
        type = fileType,
        links = standardInfo.numberOfLinks.toLong(),
        usedId = 0L,
        groupId = 0L,
        specialFileDeviceId = 0,
        size = standardInfo.endOfFile,
        blockSize = blockSize,
        blocks = (standardInfo.allocationSize + blockSize - 1) / blockSize,
        accessTime = basicInfo.lastAccessTime,
        modificationTime = basicInfo.lastWriteTime,
        changeStatusTime = basicInfo.changeTime,
    ).right()
}
