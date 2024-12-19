/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_ONLY
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import at.released.weh.filesystem.windows.win32api.windowsCreateHardLink
import platform.windows.HANDLE

internal class WindowsHardlink(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> = either {
        val newPath = pathResolver.getWindowsPath(input.newBaseDirectory, input.newPath).bind()
        return pathResolver.executeWithOpenFileHandle(
            baseDirectory = input.oldBaseDirectory,
            path = input.oldPath,
            followSymlinks = input.followSymlinks,
            access = READ_ONLY,
            errorMapper = ::openErrorToHardlinkError,
        ) { oldHandle ->
            createHardlink(oldHandle, newPath)
        }
    }

    private fun createHardlink(
        oldHandle: HANDLE,
        newPath: WindowsRealPath,
    ): Either<HardlinkError, Unit> = oldHandle.getFinalPath()
        .mapLeft { it.toResolveRelativePathError() }
        .flatMap { oldPath -> windowsCreateHardLink(newPath, oldPath) }

    @Suppress("CyclomaticComplexMethod")
    private fun openErrorToHardlinkError(error: OpenError): HardlinkError = when (error) {
        is AccessDenied -> error
        is Again -> IoError(error.message)
        is BadFileDescriptor -> error
        is DiskQuota -> error
        is Exists -> error
        is Interrupted -> IoError(error.message)
        is InvalidArgument -> error
        is IoError -> error
        is Mfile -> IoError(error.message)
        is Mlink -> IoError(error.message)
        is NameTooLong -> error
        is Nfile -> IoError(error.message)
        is NoEntry -> error
        is NoSpace -> error
        is NotCapable -> error
        is NotDirectory -> error
        is NotSupported -> error
        is Nxio -> IoError(error.message)
        is PathIsDirectory -> IoError(error.message)
        is PermissionDenied -> error
        is ReadOnlyFileSystem -> error
        is TextFileBusy -> IoError(error.message)
        is TooManySymbolicLinks -> error
    }
}
