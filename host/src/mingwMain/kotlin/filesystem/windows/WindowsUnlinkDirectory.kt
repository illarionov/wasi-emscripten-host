/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.windows.WindowsUnlinkFile.Companion.openErrorToUnlinkError
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_WRITE_DELETE
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import at.released.weh.filesystem.windows.win32api.windowsRemoveDirectory
import platform.windows.HANDLE

internal class WindowsUnlinkDirectory(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<UnlinkDirectory, UnlinkError, Unit> {
    override fun invoke(input: UnlinkDirectory): Either<UnlinkError, Unit> {
        return pathResolver.executeWithOpenFileHandle(
            baseDirectory = input.baseDirectory,
            path = input.path,
            followSymlinks = false,
            access = READ_WRITE_DELETE,
            errorMapper = { openErrorToUnlinkError(input.path, it) },
            block = ::deleteDirectoryByHandle,
        )
    }
}

private fun deleteDirectoryByHandle(
    handle: HANDLE,
): Either<UnlinkError, Unit> = either {
    val info = handle.getFileAttributeTagInfo()
        .mapLeft(WindowsUnlinkFile::statErrorToUnlinkError)
        .bind()

    // UnlinkDirectory should not work for symlinks
    if (!info.fileAttributes.isDirectory || info.fileAttributes.isSymlinkOrReparsePoint) {
        raise(NotDirectory("Path is not a directory"))
    }

    val finalPath: WindowsRealPath = handle.getFinalPath()
        .mapLeft { it.toResolveRelativePathError() }
        .bind()

    // Using RemoveDirectoryW instead of setFileDispositionInfoEx because it returns an error if the directory
    // is not empty
    return windowsRemoveDirectory(finalPath)
}
