/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter
import at.released.weh.filesystem.path.toGetCwdError
import at.released.weh.filesystem.path.toResolvePathError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath

internal class WindowsGetCurrentWorkingDirectory(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<GetCurrentWorkingDirectory, GetCurrentWorkingDirectoryError, VirtualPath> {
    override fun invoke(input: GetCurrentWorkingDirectory): Either<GetCurrentWorkingDirectoryError, VirtualPath> {
        return pathResolver.getBaseDirectory(CurrentWorkingDirectory)
            .mapLeft<GetCurrentWorkingDirectoryError>(ResolvePathError::toGetCwdError)
            .flatMap { cwdChannel: WindowsDirectoryChannel? ->
                when {
                    cwdChannel != null -> cwdChannel.handle.getFinalPath()
                        .mapLeft<GetCurrentWorkingDirectoryError>(GetFinalPathError::toGetCwdError)
                        .flatMap { windowsRealPath ->
                            WindowsPathConverter.toVirtualPath(windowsRealPath)
                                .mapLeft { it.toResolvePathError().toGetCwdError() }
                        }

                    else -> NoEntry("Current directory not set").left()
                }
            }
    }
}

private fun GetFinalPathError.toGetCwdError(): GetCurrentWorkingDirectoryError = when (this) {
    is GetFinalPathError.AccessDenied -> AccessDenied("Access denied")
    is GetFinalPathError.InvalidHandle -> AccessDenied("Invalid handle")
    is GetFinalPathError.InvalidPathFormat -> InvalidArgument("Invalid argument")
    is GetFinalPathError.MaxAttemptsReached -> NameTooLong("Current working directory name too long")
    is GetFinalPathError.OtherError -> InvalidArgument("Error `${this.message}`")
}
