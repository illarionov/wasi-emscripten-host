/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.pathresolver.resolveRealPath
import at.released.weh.filesystem.windows.win32api.windowsDeleteFile
import platform.windows.PathIsDirectoryW

internal class WindowsUnlinkFile(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> {
        return fsState.pathResolver.resolveRealPath(input.baseDirectory, input.path)
            .flatMap {
                if (PathIsDirectoryW(it) != 0) {
                    PathIsDirectory("Path is a directory").left()
                } else {
                    windowsDeleteFile(it)
                }
            }
    }
}
