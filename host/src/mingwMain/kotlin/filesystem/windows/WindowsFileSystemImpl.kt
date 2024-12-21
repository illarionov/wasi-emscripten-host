/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.filesystem.op.chmod.ChmodFd
import at.released.weh.filesystem.op.chown.Chown
import at.released.weh.filesystem.op.chown.ChownFd
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.op.fadvise.FadviseFd
import at.released.weh.filesystem.op.fallocate.FallocateFd
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.fdrenumber.Fdrenumber
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.op.setfdflags.SetFdFlags
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: String?,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = WindowsFileSystemState.create(
        stdio = stdio,
        isRootAccessAllowed = isRootAccessAllowed,
        cwd = currentWorkingDirectory ?: "",
        preopenedDirectories = preopenedDirectories,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to WindowsOpen(fsState),
        CloseFd to WindowsCloseFd(fsState),
        AddAdvisoryLockFd to WindowsAddAdvisoryLockFd(fsState),
        RemoveAdvisoryLockFd to WindowsRemoveAdvisoryLockFd(fsState),
        CheckAccess to WindowsCheckAccess(fsState.pathResolver),
        Chmod to WindowsChmod(fsState.pathResolver),
        ChmodFd to WindowsChmodFd(fsState),
        Chown to WindowsChown(fsState.pathResolver),
        ChownFd to WindowsChownFd(fsState),
        FadviseFd to WindowsFadviseFd(fsState),
        FallocateFd to WindowsFallocate(fsState),
        FdAttributes to WindowsFdAttributes(fsState),
        Fdrenumber to WindowsFdrenumber(fsState),
        GetCurrentWorkingDirectory to WindowsGetCurrentWorkingDirectory(fsState.pathResolver),
        Hardlink to WindowsHardlink(fsState.pathResolver),
        Mkdir to WindowsMkdir(fsState.pathResolver),
        PrestatFd to WindowsPrestatFd(fsState),
        Symlink to WindowsSymlink(fsState.pathResolver),
        ReadFd to WindowsReadFd(fsState),
        ReadDirFd to WindowsReadDirFd(fsState),
        ReadLink to WindowsReadLink(fsState.pathResolver),
        Rename to WindowsRename(fsState.pathResolver),
        SeekFd to WindowsSeekFd(fsState),
        SetFdFlags to WindowsSetFdFlags(fsState),
        SetTimestamp to WindowsSetTimestamp(fsState.pathResolver),
        SetTimestampFd to WindowsSetTimestampFd(fsState),
        Stat to WindowsStat(fsState.pathResolver),
        StatFd to WindowsStatFd(fsState),
        SyncFd to WindowsSync(fsState),
        TruncateFd to WindowsTruncateFd(fsState),
        UnlinkFile to WindowsUnlinkFile(fsState.pathResolver),
        UnlinkDirectory to WindowsUnlinkDirectory(fsState.pathResolver),
        WriteFd to WindowsWriteFd(fsState),
    )
    private val fsAdapter = DelegateOperationsFileSystem(operations, interceptors)

    override fun close() {
        fsState.close()
    }

    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> = fsAdapter.execute(operation, input)

    override fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean {
        return fsAdapter.isOperationSupported(operation)
    }
}
