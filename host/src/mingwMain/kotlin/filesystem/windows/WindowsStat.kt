/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
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
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_ONLY
import at.released.weh.filesystem.windows.nativefunc.open.executeWithOpenFileHandle
import at.released.weh.filesystem.windows.nativefunc.stat.windowsStatFd

internal class WindowsStat(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<Stat, StatError, StructStat> {
    override fun invoke(input: Stat): Either<StatError, StructStat> = fsState.executeWithOpenFileHandle(
        baseDirectory = input.baseDirectory,
        path = input.path,
        followSymlinks = input.followSymlinks,
        access = READ_ONLY,
        errorMapper = { it.toStatError() },
    ) { handle -> windowsStatFd(handle) }
}

@Suppress("CyclomaticComplexMethod")
private fun OpenError.toStatError(): StatError = when (this) {
    is AccessDenied -> this
    is Again -> IoError(this.message)
    is BadFileDescriptor -> this
    is DiskQuota -> IoError(this.message)
    is Exists -> IoError(this.message)
    is Interrupted -> IoError(this.message)
    is InvalidArgument -> this
    is IoError -> this
    is Mfile -> IoError(this.message)
    is Mlink -> IoError(this.message)
    is NameTooLong -> this
    is Nfile -> IoError(this.message)
    is NoEntry -> this
    is NoSpace -> IoError(this.message)
    is NotCapable -> this
    is NotDirectory -> this
    is NotSupported -> IoError(this.message)
    is Nxio -> IoError(this.message)
    is PathIsDirectory -> IoError(this.message)
    is PermissionDenied -> IoError(this.message)
    is ReadOnlyFileSystem -> IoError(this.message)
    is TextFileBusy -> IoError(this.message)
    is TooManySymbolicLinks -> this
}
