/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
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
import at.released.weh.filesystem.op.poll.Poll
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

internal class LinuxFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: CurrentWorkingDirectoryConfig,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = LinuxFileSystemState.create(
        stdio = stdio,
        currentWorkingDirectory = currentWorkingDirectory,
        preopenedDirectories = preopenedDirectories,
        isRootAccessAllowed = isRootAccessAllowed,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to LinuxOpen(fsState, fsState.fsExecutor, isRootAccessAllowed),
        CloseFd to LinuxCloseFd(fsState),
        AddAdvisoryLockFd to LinuxAddAdvisoryLockFd(fsState),
        RemoveAdvisoryLockFd to LinuxRemoveAdvisoryLockFd(fsState),
        CheckAccess to LinuxCheckAccess(fsState.fsExecutor),
        Chmod to LinuxChmod(fsState.fsExecutor),
        ChmodFd to LinuxChmodFd(fsState),
        Chown to LinuxChown(fsState.fsExecutor),
        ChownFd to LinuxChownFd(fsState),
        FadviseFd to LinuxFadviseFd(fsState),
        FallocateFd to LinuxFallocate(fsState),
        FdAttributes to LinuxFdAttributes(fsState),
        Fdrenumber to LinuxFdrenumber(fsState),
        GetCurrentWorkingDirectory to LinuxGetCurrentWorkingDirectory(fsState.pathResolver),
        Hardlink to LinuxHardlink(fsState.fsExecutor),
        Mkdir to LinuxMkdir(fsState.fsExecutor),
        Poll to LinuxPoll(fsState),
        PrestatFd to LinuxPrestatFd(fsState),
        Symlink to LinuxSymlink(fsState.fsExecutor),
        ReadFd to LinuxReadFd(fsState),
        ReadDirFd to LinuxReadDirFd(fsState),
        ReadLink to LinuxReadLink(fsState.fsExecutor),
        Rename to LinuxRename(fsState.fsExecutor),
        SeekFd to LinuxSeekFd(fsState),
        SetFdFlags to LinuxSetFdFlags(fsState),
        SetTimestamp to LinuxSetTimestamp(fsState.fsExecutor),
        SetTimestampFd to LinuxSetTimestampFd(fsState),
        Stat to LinuxStat(fsState.fsExecutor),
        StatFd to LinuxStatFd(fsState),
        SyncFd to LinuxSync(fsState),
        TruncateFd to LinuxTruncateFd(fsState),
        UnlinkFile to LinuxUnlinkFile(fsState.fsExecutor),
        UnlinkDirectory to LinuxUnlinkDirectory(fsState.fsExecutor),
        WriteFd to LinuxWriteFd(fsState),
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
