/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readdir

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.windows.nativefunc.open.useFileForAttributeAccess
import at.released.weh.filesystem.windows.win32api.ext.fromAttributes
import at.released.weh.filesystem.windows.win32api.ext.get64bitInode
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileIdInfo
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import at.released.weh.filesystem.windows.win32api.model.ReparseTag
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.HANDLE
import platform.windows.WIN32_FIND_DATAW

internal fun readDirEntry(
    rootdir: HANDLE,
    rootdirPath: WindowsRealPath,
    data: WIN32_FIND_DATAW,
): Either<ReadDirError, DirEntry> = either {
    val fileName = WindowsRealPath.create(data.cFileName.toKStringFromUtf16())
        .getOrElse { error -> return AccessDenied(error.message).left() }
    val (baseHandle: HANDLE?, realPath: WindowsRealPath) = if (fileName.kString != "..") {
        rootdir to fileName
    } else {
        val newPath: WindowsRealPath = rootdirPath.parent ?: rootdirPath
        null to newPath
    }

    val fileInfo = useFileForAttributeAccess(
        baseHandle = baseHandle,
        path = realPath,
        followSymlinks = false,
        errorMapper = { it.toReadDirError() },
    ) { fileHandle -> fileHandle.getFileIdInfo().mapLeft(StatError::toReadDirError) }.bind()

    val filetype = Filetype.fromAttributes(FileAttributes(data.dwFileAttributes), ReparseTag(data.dwReserved0))

    DirEntry(fileName.kString, filetype, fileInfo.get64bitInode(), 0)
}
